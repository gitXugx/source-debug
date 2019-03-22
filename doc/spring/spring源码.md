# spring源码

## BeanFactoy

**环境信息**
jdk1.8
maven3.0
spring:4.3.5


下面是 `POM` 文件所需要的jar包:

**pom.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>mybatis-debug</artifactId>
        <groupId>com.xugx.github</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>spring-debug-source</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>4.3.5.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-orm</artifactId>
            <version>4.3.5.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
            <version>4.3.5.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>4.3.5.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-core</artifactId>
            <version>4.2.1.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-web</artifactId>
            <version>4.2.1.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-config</artifactId>
            <version>4.2.1.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-ldap</artifactId>
            <version>4.2.1.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-acl</artifactId>
            <version>4.2.1.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-cas</artifactId>
            <version>4.2.1.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-taglibs</artifactId>
            <version>4.2.1.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <version>4.3.5.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

这边是 `spring` 的配置文件

**spring-config.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="simpleBean" class="com.xugx.github.spring.debug.bean.SimpleBean" >
        <property name="name" value="xugx"/>
        <property name="id" value="10"/>
    </bean>
    <alias name="simpleBean" alias="simpleBean2"/>
</beans>
```

下面是测试类，之后的debug就从这个测试类开始

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

可以看到首先是读成spring的资源 `ClassPathResource` 然后根据资源解析创建 `beanFactoy` ，那从 `ClassPathResource`看起。


**Resource**
Spring 对资源进行了抽象，使其可以进行统一操作。


- [x] springResource图片

可以看到所有的资源都会最终统一成为二进制流。而 `Resource` 定义一些对资源描述访问的接口。特定的资源由特定的实现来将进行控制。这些`UrlResource` 、`ClassPathResource`、`FileSystemResource`、`PathResource` 、`ByteArrayResource` 、`InputStreamResource` 这些都是它的实现。`BeanFactoy` 主要使用的是 `ClassPathResource` 来进行读取 classpath下的文件



**XmlBeanFactory**

- [x] springXmlBeanFactory图片

上面图片是 `XmlBeanFactory` 的UML图，可以看到XmlBeanFactory有3个顶级接口
1. `AliasRegistry` 用于管理别名的顶级接口
2. `SingletonBeanRegistry` 单例注册管理器，提供单例注册接口、单例bean统计等接口。
3. `BeanFactory` 是spring提供的bean基本访问视图，提供一系列的bean处理方法

那意味着 `XmlBeanFactory` 具有上面接口的所有功能，并且具有很多的扩展功能。

```java
public class XmlBeanFactory extends DefaultListableBeanFactory {
	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);
	public XmlBeanFactory(Resource resource) throws BeansException {
		this(resource, null);
	}
    //调用父类方法，设置当前容器为父容器
	public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
        //调用XmlBeanDefinitionReader的加载beanDefinitions方法
		this.reader.loadBeanDefinitions(resource);
	}
}
```


`XmlBeanDefinitionReader` 在创建的时候进行了初始化，下面是初始化的具体逻辑。

```java
public abstract class AbstractBeanDefinitionReader implements EnvironmentCapable, BeanDefinitionReader {
    //因为BeanFactory实现了BeanDefinitionRegistry用于bean描述定义的注册器
	protected AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;

		// Determine ResourceLoader to use.
		if (this.registry instanceof ResourceLoader) {
			this.resourceLoader = (ResourceLoader) this.registry;
		}
		else {
            //设置resourceLoader，为classpath为加载的路径
			this.resourceLoader = new PathMatchingResourcePatternResolver();
		}

		// Inherit Environment if possible
		if (this.registry instanceof EnvironmentCapable) {
			this.environment = ((EnvironmentCapable) this.registry).getEnvironment();
		}
		else {
            //设置环境信息
			this.environment = new StandardEnvironment();
		}
	}
}
```

`XmlBeanDefinitionReader` 初始化的时候主要做了两件事:
1. 设置资源加载的路径
2. 设置系统的环境变量，以properties方式

调用 `loadBeanDefinitions`方法进行加载 `beanDefiniition`

```java
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

    public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
		Assert.notNull(encodedResource, "EncodedResource must not be null");
		if (logger.isInfoEnabled()) {
			logger.info("Loading XML bean definitions from " + encodedResource.getResource());
		}
        //先把需要加载的资源保存到该线程的ThreadLocal中
		Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
		if (currentResources == null) {
			currentResources = new HashSet<EncodedResource>(4);
			this.resourcesCurrentlyBeingLoaded.set(currentResources);
		}
        //添加失败有可能已经在解析了
		if (!currentResources.add(encodedResource)) {
			throw new BeanDefinitionStoreException(
					"Detected cyclic loading of " + encodedResource + " - check your import definitions!");
		}
		try {
            //获取该文件的二进制流
			InputStream inputStream = encodedResource.getResource().getInputStream();
			try {
				InputSource inputSource = new InputSource(inputStream);
				if (encodedResource.getEncoding() != null) {
					inputSource.setEncoding(encodedResource.getEncoding());
				}
                //实际解析的工作
				return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
			}
			finally {
				inputStream.close();
			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException(
					"IOException parsing XML document from " + encodedResource.getResource(), ex);
		}
		finally {
            //不论加载成功还是失败，都移除该配置文件
			currentResources.remove(encodedResource);
			if (currentResources.isEmpty()) {
                //如果没有加载的配置文件，则释放该set对象。
				this.resourcesCurrentlyBeingLoaded.remove();
			}
		}
	}
}
```
上面主要把文件解析合适的资源和防止同一个线程重复加载配置文件。实际加载配置文件的方法是 `doLoadBeanDefinitions`

```java
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {
	protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
			throws BeanDefinitionStoreException {
		try {
            //解析成doc对象，实际上是委托给DefaultDocumentLoader去解析。
			Document doc = doLoadDocument(inputSource, resource);
            //根据解析的doc对象去注册bean
			return registerBeanDefinitions(doc, resource);
		}
		catch (BeanDefinitionStoreException ex) {
			throw ex;
		}
		catch (SAXParseException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(),
					"Line " + ex.getLineNumber() + " in XML document from " + resource + " is invalid", ex);
		}
		catch (SAXException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(),
					"XML document from " + resource + " is invalid", ex);
		}
		catch (ParserConfigurationException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"Parser configuration exception parsing XML from " + resource, ex);
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"IOException parsing XML document from " + resource, ex);
		}
		catch (Throwable ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"Unexpected exception parsing XML document from " + resource, ex);
		}
	}
}
```
调用了两个重要的方法 `doLoadDocument` 和 `registerBeanDefinitions` 去做xml解析成doc对象和做beanDefinition注册，下面有很多异常，其中有个 `SAXException` 可以看出xml的解析使用的是 `sax`, 下面主要看的是 `registerBeanDefinitions`

```java
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {
	public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
        //使用反射创建一个解析beanDefinitionDoc的对象 BeanDefinitionDocumentReader 
		BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
        //获取当前容器中已经注册过的bean
		int countBefore = getRegistry().getBeanDefinitionCount();
        //进行注册beanDefinitions
		documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
        //返回注册成功多少bean
		return getRegistry().getBeanDefinitionCount() - countBefore;
	}
}
```
大致上到这里 `XmlBeanDefinitionReader` 的执行就结束了。主要做了3件事

1. 初始化资源和资源加载路径 
2. 委托xml的解析工作给 `DefaultDocumentLoader`
3. 把 `document` 对象中xml, 注册成beanDefinition的工作委托给 `DefaultBeanDefinitionDocumentReader`

```java
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {
	@Override
	public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
		this.readerContext = readerContext;
		logger.debug("Loading bean definitions");
        //获取xml的根元素<beans>
		Element root = doc.getDocumentElement();
        //做注册beanDefinitions
		doRegisterBeanDefinitions(root);
	}

    //在根元素<beans> 注册bean元素
	protected void doRegisterBeanDefinitions(Element root) {
		// Any nested <beans> elements will cause recursion in this method. In
		// order to propagate and preserve <beans> default-* attributes correctly,
		// keep track of the current (parent) delegate, which may be null. Create
		// the new (child) delegate with a reference to the parent for fallback purposes,
		// then ultimately reset this.delegate back to its original (parent) reference.
		// this behavior emulates a stack of delegates without actually necessitating one.
		BeanDefinitionParserDelegate parent = this.delegate;
        //填充解析beans的默认属性，然后调用监听器
		this.delegate = createDelegate(getReaderContext(), root, parent);

		if (this.delegate.isDefaultNamespace(root)) {
			String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
			if (StringUtils.hasText(profileSpec)) {
				String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
						profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
				if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
					if (logger.isInfoEnabled()) {
						logger.info("Skipped XML bean definition file due to specified profiles [" + profileSpec +
								"] not matching: " + getReaderContext().getResource());
					}
					return;
				}
			}
		}

		preProcessXml(root);
		parseBeanDefinitions(root, this.delegate);
		postProcessXml(root);
		this.delegate = parent;
	}
}
```
















































