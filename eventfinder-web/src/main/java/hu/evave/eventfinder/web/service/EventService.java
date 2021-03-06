package hu.evave.eventfinder.web.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.evave.eventfinder.web.mapper.EventFinderMapper;
import hu.evave.eventfinder.web.model.Event;
import hu.evave.eventfinder.web.model.Location;
import hu.evave.eventfinder.web.model.user.User;
import hu.evave.eventfinder.web.model.user.UserSettings;
import hu.evave.eventfinder.web.repository.EventRepository;
import hu.evave.eventfinder.web.repository.UserRepository;

@Service
public class EventService {

	private static final int CONSTANT = 5;

	@Autowired
	EventRepository eventRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	EventFinderMapper eventMapper;

	@Autowired
	EmailService emailService;

	public List<Event> getEventsByKeyword(String keyword) {
		if (keyword != null && !keyword.isEmpty()) {
			return eventRepository.findByNameContaining(keyword);
		}

		return null;
	}

	@Scheduled(cron = "0 5 * * * *")
	@Transactional
	public void sendScheduledNotification() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, CONSTANT);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Date startFrom = cal.getTime();

		cal.add(Calendar.HOUR, 1);
		cal.add(Calendar.MILLISECOND, -1);

		Date startTo = cal.getTime();
		sendNotificationByDate(startFrom, startTo);
	}

	private void sendNotificationByDate(Date startFrom, Date startTo) {

		List<Event> eventsToNotify = eventRepository.findByStartsAtBetween(startFrom, startTo);

		Map<User, List<Event>> eventsByUsers = new HashMap<>();

		for (Event event : eventsToNotify) {
			List<User> users = event.getSubscribedUsers();
			addUsersToNotifications(eventsByUsers, users, event);
		}

		for (Entry<User, List<Event>> eventsByUser : eventsByUsers.entrySet()) {
			String message = "Dear User!\nThe following events are coming:\n";
			for (Event event : eventsByUser.getValue()) {
				message += event.getName();
				message += "\n";
			}
			emailService.prepareAndSend(eventsByUser.getKey().getEmail(), message);
		}
	}

	private void addUsersToNotifications(Map<User, List<Event>> eventsByUsers, List<User> users, Event event) {
		for (User user : users) {
			addUserToNotifications(eventsByUsers, user, event);
		}
	}

	private void addUserToNotifications(Map<User, List<Event>> eventsByUsers, User user, Event event) {
		final List<Event> events;
		if (!eventsByUsers.containsKey(user)) {
			events = new ArrayList<>();
			eventsByUsers.put(user, events);
		} else {
			events = eventsByUsers.get(user);
		}

		events.add(event);
	}

	@Transactional
	public void sendCreateNotification(Event event) {
		sendLocationNotification(event);
		sendAdvertiserNotification(event);
	}

	private void sendAdvertiserNotification(Event event) {
		User advertiser = event.getCreatedBy();
		List<User> users = advertiser.getSubscribedUsers();

		for (User user : users) {
			UserSettings settings = user.getSettings();
			if (settings.isEmailNotificationEnabled() && settings.isAdvertiserNotificationEnabled()) {
				String message = "Dear " + user.getName() + "!\n\nYou might be interested in the following event created by " + advertiser.getName() + ": \n"
						+ event.getName() + "\n" + event.getLocation().getName();
				emailService.prepareAndSend(user.getEmail(), message);
			}
		}
	}

	private void sendLocationNotification(Event event) {
		Location location = event.getLocation();
		List<User> users = location.getSubscribedUsers();

		for (User user : users) {
			UserSettings settings = user.getSettings();
			if (settings.isEmailNotificationEnabled() && settings.isAdvertiserNotificationEnabled()) {
				String message = "Dear " + user.getName() + "!\n\nYou might be interested in the following event taken place in " + location.getName() + ": \n"
						+ event.getName() + "\n" + event.getLocation().getAddress();
				emailService.prepareAndSend(user.getEmail(), message);
			}
		}
	}

	public List<Event> getMyEvents() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userRepository.findByName(auth.getName());
		return eventRepository.findByCreatedBy(user);
	}

	public void deleteEvent(Event eventToDelete) {
		// valójában nem töröljük ki az adatbázisból
		eventToDelete.setStartsAt(null);
		eventRepository.save(eventToDelete);
	}

	public Event createEmptyEvent() {
		Event event = new Event();
		event.setId(-1L);
		event.setLocation(new Location());
		event.setTypes(new ArrayList<>());
		return event;
	}

	public void corrigateTime(Event event) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(event.getStartsAt());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setStartsAt(cal.getTime());

		cal.setTime(event.getEndsAt());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setEndsAt(cal.getTime());
	}

	public void createNewEvent(Event event) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		event.setCreatedBy(userRepository.findByName(auth.getName()));

		if (event.getId() != null && event.getId() < 0) {
			event.setId(null);
		}

		corrigateTime(event);

		eventRepository.save(event);

		sendCreateNotification(event);
	}

	public void editEvent(Event event) {
		corrigateTime(event);
		eventRepository.save(event);
	}

}
