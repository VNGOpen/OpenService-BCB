package modules.registry;

import akka.actor.ActorSystem;
import api.FrontendApi;
import bo.IBcbDao;
import play.Application;

public interface IRegistry {

    /**
     * Gets the current running Play application.
     * 
     * @return
     */
    public Application getPlayApplication();

    /**
     * Gets the {@link ActorSystem} instance.
     * 
     * @return
     */
    public ActorSystem getActorSystem();

    /**
     * Gets a Spring bean by class.
     * 
     * @param clazz
     * @return
     */
    public <T> T getBean(Class<T> clazz);

    /**
     * Gets a Spring bean by name and class.
     * 
     * @param name
     * @param clazz
     * @return
     */
    public <T> T getBean(String name, Class<T> clazz);

    public FrontendApi getFrontendApi();
    
    public IBcbDao getBcbDao();
}
