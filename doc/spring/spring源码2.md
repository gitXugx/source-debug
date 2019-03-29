# spring源码
> 前面大致看了一下获取`xmlBeanFactory`,下面看bean的获取
## 获取bean

环境与上篇一致

```java
public class BeanFactoryTest {

    @Test
    public void testSimpleBean(){
        XmlBeanFactory xmlBeanFactory = new XmlBeanFactory(new ClassPathResource("spring-config.xml"));

        Object simpleBean = xmlBeanFactory.getBean("simpleBean2");

        System.out.println(simpleBean);
    }
}
```
获取Bean主要是 `xmlBeanFactory.getBean("simpleBean2");`

```java
	protected <T> T doGetBean(
			final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly)
			throws BeansException {
        //通过nanme获取到实际beanName
		final String beanName = transformedBeanName(name);
		Object bean;

		// 获取早已暴漏的对象
		Object sharedInstance = getSingleton(beanName);
        //不等于空证明sharedInstance已经处理好了
		if (sharedInstance != null && args == null) {
			if (logger.isDebugEnabled()) {
                //如果还在创建说明循环引用了
				if (isSingletonCurrentlyInCreation(beanName)) {
					logger.debug("Returning eagerly cached instance of singleton bean '" + beanName +
							"' that is not fully initialized yet - a consequence of a circular reference");
				}
				else {
					logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
				}
			}
            //对factorybean的处理，返回工厂对象创建的bean
			bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
		}

		else {
            //原型模式出现循环依赖则无法处理，则抛出异常
			if (isPrototypeCurrentlyInCreation(beanName)) {
				throw new BeanCurrentlyInCreationException(beanName);
			}

			// Check if bean definition exists in this factory.
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// Not found -> check parent.
				String nameToLookup = originalBeanName(name);
				if (args != null) {
					// Delegation to parent with explicit args.
					return (T) parentBeanFactory.getBean(nameToLookup, args);
				}
				else {
					// No args -> delegate to standard getBean method.
					return parentBeanFactory.getBean(nameToLookup, requiredType);
				}
			}

			if (!typeCheckOnly) {
				markBeanAsCreated(beanName);
			}

			try {
				final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				checkMergedBeanDefinition(mbd, beanName, args);

				// Guarantee initialization of beans that the current bean depends on.
				String[] dependsOn = mbd.getDependsOn();
				if (dependsOn != null) {
					for (String dep : dependsOn) {
						if (isDependent(beanName, dep)) {
							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
									"Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
						}
						registerDependentBean(dep, beanName);
						getBean(dep);
					}
				}

				// Create bean instance.
				if (mbd.isSingleton()) {
					sharedInstance = getSingleton(beanName, new ObjectFactory<Object>() {
						@Override
						public Object getObject() throws BeansException {
							try {
								return createBean(beanName, mbd, args);
							}
							catch (BeansException ex) {
								// Explicitly remove instance from singleton cache: It might have been put there
								// eagerly by the creation process, to allow for circular reference resolution.
								// Also remove any beans that received a temporary reference to the bean.
								destroySingleton(beanName);
								throw ex;
							}
						}
					});
					bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
				}

				else if (mbd.isPrototype()) {
					// It's a prototype -> create a new instance.
					Object prototypeInstance = null;
					try {
						beforePrototypeCreation(beanName);
						prototypeInstance = createBean(beanName, mbd, args);
					}
					finally {
						afterPrototypeCreation(beanName);
					}
					bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
				}

				else {
					String scopeName = mbd.getScope();
					final Scope scope = this.scopes.get(scopeName);
					if (scope == null) {
						throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
					}
					try {
						Object scopedInstance = scope.get(beanName, new ObjectFactory<Object>() {
							@Override
							public Object getObject() throws BeansException {
								beforePrototypeCreation(beanName);
								try {
									return createBean(beanName, mbd, args);
								}
								finally {
									afterPrototypeCreation(beanName);
								}
							}
						});
						bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
					}
					catch (IllegalStateException ex) {
						throw new BeanCreationException(beanName,
								"Scope '" + scopeName + "' is not active for the current thread; consider " +
								"defining a scoped proxy for this bean if you intend to refer to it from a singleton",
								ex);
					}
				}
			}
			catch (BeansException ex) {
				cleanupAfterBeanCreationFailure(beanName);
				throw ex;
			}
		}

		// Check if required type matches the type of the actual bean instance.
		if (requiredType != null && bean != null && !requiredType.isAssignableFrom(bean.getClass())) {
			try {
				return getTypeConverter().convertIfNecessary(bean, requiredType);
			}
			catch (TypeMismatchException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to convert bean '" + name + "' to required type '" +
							ClassUtils.getQualifiedName(requiredType) + "'", ex);
				}
				throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
			}
		}
		return (T) bean;
	}

```




```java
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {
	protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        //从单例缓存中获取实例对象
		Object singletonObject = this.singletonObjects.get(beanName);
        //缓存未命中，并且该bean在在创建中
		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            //在创建Singleton的时候首先获取singletonObjects锁，所以如果在创建中，这边会处于阻塞状态
			synchronized (this.singletonObjects) {
                //获取提前暴露的单例对象
				singletonObject = this.earlySingletonObjects.get(beanName);
                //如果获取的对象为null , 允许提前 暴露
				if (singletonObject == null && allowEarlyReference) {
                    //获取单例工厂里面的对象，然后移除singletonFactories工厂
					ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
					if (singletonFactory != null) {
						singletonObject = singletonFactory.getObject();
                        //加入提前暴露的单例
						this.earlySingletonObjects.put(beanName, singletonObject);
						this.singletonFactories.remove(beanName);
					}
				}
			}
		}
        //该单例还未创建返回null
		return (singletonObject != NULL_OBJECT ? singletonObject : null);
	}
}
```


```java

    protected Object getObjectForBeanInstance(
            Object beanInstance, String name, String beanName, RootBeanDefinition mbd) {

        // Don't let calling code try to dereference the factory if the bean isn't a factory.
        if (BeanFactoryUtils.isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
            throw new BeanIsNotAFactoryException(transformedBeanName(name), beanInstance.getClass());
        }
        //如果不是FactoryBean或者name以“&”开始，则返回 beanInstance,证明是正常的bean
        if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {
            return beanInstance;
        }

        //通过FactoryBean获取bean实例
        Object object = null;
        if (mbd == null) {
            object = getCachedObjectForFactoryBean(beanName);
        }
        //如果为空，有可能是多例模式，或者单例对象第一次初始化
        if (object == null) {
            // Return bean instance from factory.
            FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
            // Caches object obtained from FactoryBean if it is a singleton.
            if (mbd == null && containsBeanDefinition(beanName)) {
                mbd = getMergedLocalBeanDefinition(beanName);
            }
            boolean synthetic = (mbd != null && mbd.isSynthetic());
            //调用getobject方法获取bean，在对缓存进行put的时候都需要进行加锁，以防覆盖的情况，执行postprocessafter方法，如果是单例则缓存到factorybeanobject中去，
            object = getObjectFromFactoryBean(factory, beanName, !synthetic);
        }
        return object;
    }

```




















