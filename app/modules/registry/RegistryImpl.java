package modules.registry;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import akka.actor.ActorSystem;
import api.FrontendApi;
import bo.IBcbDao;
import play.Application;
import play.Logger;
import play.inject.ApplicationLifecycle;
import utils.AppGlobals;

@Singleton
public class RegistryImpl implements IRegistry {

    private ApplicationContext appContext;
    private Application playApp;
    private ActorSystem actorSystem;

    /**
     * {@inheritDoc}
     */
    @Inject
    public RegistryImpl(ApplicationLifecycle lifecycle, Application playApp,
            ActorSystem actorSystem) {
        AppGlobals.registry = this;
        AppGlobals.appConfig = playApp.configuration();

        this.playApp = playApp;
        this.actorSystem = actorSystem;

        // for Java pre-8
        // lifecycle.addStopHook(new Callable<CompletionStage<?>>() {
        // @Override
        // public CompletionStage<?> call() throws Exception {
        // destroy();
        // return CompletableFuture.completedFuture(null);
        // }
        // });

        // for Java 8+
        lifecycle.addStopHook(() -> {
            destroy();
            return CompletableFuture.completedFuture(null);
        });

        try {
            init();
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
    }

    private void initApplicationContext() {
        String configFile = playApp.configuration().getString("spring.conf");
        if (StringUtils.isEmpty(configFile)) {
            Logger.warn("No [spring.conf] defined!");
            return;
        }
        File springConfigFile = configFile.startsWith("/") ? new File(configFile)
                : new File(playApp.path(), configFile);
        if (springConfigFile.isFile() && springConfigFile.canRead()) {
            Logger.info("Loading Spring bean config from [" + springConfigFile + "]...");
            AbstractApplicationContext applicationContext = new FileSystemXmlApplicationContext(
                    "file:" + springConfigFile.getAbsolutePath());
            applicationContext.start();
            appContext = applicationContext;
        } else {
            Logger.error("[" + springConfigFile + "] is invalid or not readable!");
        }
    }

    private void init() throws Exception {
        initApplicationContext();
    }

    private void destroyApplicationContext() {
        if (appContext != null) {
            try {
                ((AbstractApplicationContext) appContext).destroy();
            } catch (Exception e) {
                Logger.warn(e.getMessage(), e);
            } finally {
                appContext = null;
            }
        }
    }

    private void destroy() {
        destroyApplicationContext();
        actorSystem = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Application getPlayApplication() {
        return playApp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getBean(Class<T> clazz) {
        try {
            return appContext.getBean(clazz);
        } catch (NoUniqueBeanDefinitionException e1) {
            Logger.error(e1.getMessage(), e1);
            return null;
        } catch (NoSuchBeanDefinitionException e2) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getBean(String name, Class<T> clazz) {
        try {
            return appContext.getBean(name, clazz);
        } catch (BeanNotOfRequiredTypeException e1) {
            Logger.error(e1.getMessage(), e1);
            return null;
        } catch (NoSuchBeanDefinitionException e2) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FrontendApi getFrontendApi() {
        return getBean(FrontendApi.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IBcbDao getBcbDao() {
        return getBean(IBcbDao.class);
    }
}
