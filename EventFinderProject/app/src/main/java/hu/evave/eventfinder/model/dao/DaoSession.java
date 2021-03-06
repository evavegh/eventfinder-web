package hu.evave.eventfinder.model.dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import hu.evave.eventfinder.model.Event;
import hu.evave.eventfinder.model.EventTypeMapping;
import hu.evave.eventfinder.model.Location;
import hu.evave.eventfinder.model.Price;

import hu.evave.eventfinder.model.dao.EventDao;
import hu.evave.eventfinder.model.dao.EventTypeMappingDao;
import hu.evave.eventfinder.model.dao.LocationDao;
import hu.evave.eventfinder.model.dao.PriceDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig eventDaoConfig;
    private final DaoConfig eventTypeMappingDaoConfig;
    private final DaoConfig locationDaoConfig;
    private final DaoConfig priceDaoConfig;

    private final EventDao eventDao;
    private final EventTypeMappingDao eventTypeMappingDao;
    private final LocationDao locationDao;
    private final PriceDao priceDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        eventDaoConfig = daoConfigMap.get(EventDao.class).clone();
        eventDaoConfig.initIdentityScope(type);

        eventTypeMappingDaoConfig = daoConfigMap.get(EventTypeMappingDao.class).clone();
        eventTypeMappingDaoConfig.initIdentityScope(type);

        locationDaoConfig = daoConfigMap.get(LocationDao.class).clone();
        locationDaoConfig.initIdentityScope(type);

        priceDaoConfig = daoConfigMap.get(PriceDao.class).clone();
        priceDaoConfig.initIdentityScope(type);

        eventDao = new EventDao(eventDaoConfig, this);
        eventTypeMappingDao = new EventTypeMappingDao(eventTypeMappingDaoConfig, this);
        locationDao = new LocationDao(locationDaoConfig, this);
        priceDao = new PriceDao(priceDaoConfig, this);

        registerDao(Event.class, eventDao);
        registerDao(EventTypeMapping.class, eventTypeMappingDao);
        registerDao(Location.class, locationDao);
        registerDao(Price.class, priceDao);
    }
    
    public void clear() {
        eventDaoConfig.clearIdentityScope();
        eventTypeMappingDaoConfig.clearIdentityScope();
        locationDaoConfig.clearIdentityScope();
        priceDaoConfig.clearIdentityScope();
    }

    public EventDao getEventDao() {
        return eventDao;
    }

    public EventTypeMappingDao getEventTypeMappingDao() {
        return eventTypeMappingDao;
    }

    public LocationDao getLocationDao() {
        return locationDao;
    }

    public PriceDao getPriceDao() {
        return priceDao;
    }

}
