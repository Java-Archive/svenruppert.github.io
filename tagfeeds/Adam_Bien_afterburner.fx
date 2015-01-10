<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<feed xmlns="http://www.w3.org/2005/Atom">
    <title>Adam Bien afterburner.fx</title>
    <category label="Adam Bien afterburner.fx"/>
    <updated>2015-01-10T02:39:48.886+01:00</updated>
    <id>http://rapidpm.github.io</id>
    <author>
        <name>Sven Ruppert</name>
    </author>
    <entry>
        <title>Adam Bien´s afterburner.fx internals explained</title>
        <category label="Adam Bien afterburner.fx"/>
        <category label="CDI"/>
        <category label="dependency injection"/>
        <category label="JavaFX"/>
        <category label="MVP"/>
        <category label="relection"/>
        <published>2013-12-09T00:00:00+01:00</published>
        <author>
            <name>Sven Ruppert</name>
        </author>
        <content type="html">&lt;article class="article clearfix"&gt;
							&lt;header class="article-header"&gt;
								&lt;h2 class="article-title"&gt;&lt;a href="/2013/12/09/adam-biens-afterburner.fx-internals-ex.html" rel="tag"&gt;Adam Bien´s afterburner.fx internals explained&lt;/a&gt;&lt;/h2&gt;
								&lt;p&gt;&lt;time datetime="2013-12-09"&gt;2013-12-09&lt;/time&gt; &lt;a href="/team/sven-ruppert" rel="author"&gt;from Sven Ruppert&lt;/a&gt;&lt;/p&gt;
							&lt;/header&gt;
							&lt;div class="article-content clearfix"&gt;
								&lt;div class="post-thumb"&gt;
									&lt;img src="./site/content/post_thumb.jpg" width="" height="" alt="" /&gt;
								&lt;/div&gt;
								&lt;div class="post-excerpt"&gt;Today I am writing about Adam´s framework afterburner.fx.
This is a MVP framework for dependency injection into JavaFX apps.
It is very small, containing only two classes.&lt;br /&gt;
&lt;br /&gt;
What could you do with this afternburner.fx? What are the restrictions?&lt;br /&gt;
&lt;br /&gt;
Let´s start with the project init. You will need only a normal pom.xml, plain no special libs are needed. I am using JDK8 because of the simple JavaFX config. (no config ;-) )&lt;br /&gt;
&lt;br /&gt;
Convention over Configuration:&lt;br /&gt;
CoC is the main in this framework. This means, that you don´t need to configure something. But you have to follow the base structure that this framework is expecting.&lt;br /&gt;
&lt;br /&gt;
As app-base-pkg I am using &lt;b&gt;org.rapidpm.demo.jaxenter.blog008&lt;/b&gt;. (you could get all from my git repo under&amp;nbsp;&lt;a href="https://bitbucket.org/rapidpm/jaxenter.de-0008-afterburner"&gt;https://bitbucket.org/rapidpm/jaxenter.de-0008-afterburner&lt;/a&gt;&amp;nbsp;) The main class will be Main, this is the JavaFX Application Class with the basic boostrapping intro. The sup-pkg orig contains the pkg presentation with only one GUI module called demo. For every GUI Module you will need two classes. The first one is a class with a name ending with View and the second one will end with Presenter. In our examplke you will find the two classes, DemoView and DemoPresenter.&lt;br /&gt;
&lt;br /&gt;
The DemoPresenter is nothing else as the Controller class for the View, declared inside the fxml file.&lt;br /&gt;
The fxml File itself must be named demo.fxml and at the same place as the Presenter/Controller class.&lt;br /&gt;
&lt;br /&gt;
&amp;nbsp;The DemoView is the GUI Component itself and must extend the FXMLView class from the framework.&lt;br /&gt;
&lt;br /&gt;
&lt;b&gt;The View - FXMView&amp;nbsp;&lt;/b&gt;&lt;br /&gt;
The DemoView have a default constructor, calling the init method. &lt;b&gt;init(Class clazz, String conventionalName)&amp;nbsp;&lt;/b&gt;&lt;br /&gt;
&lt;b&gt;&lt;br /&gt;&lt;/b&gt;

&lt;br /&gt;
&lt;pre class="brush: java"&gt;public FXMLView() {
    this.init(getClass(), getFXMLName());
}

private void init(Class clazz, String conventionalName) {
    final URL resource = clazz.getResource(conventionalName);
    String bundleName = getBundleName();
    ResourceBundle bundle = getResourceBundle(bundleName);
    this.loader = new FXMLLoader(resource, bundle);
    this.loader.setControllerFactory(new Callback&lt;class&gt;, Object&amp;gt;() {
        @Override
        public Object call(Class p) {
        return InjectionProvider.instantiatePresenter(p);
        }
        });
        try {
        loader.load();
        } catch (Exception ex) {
        throw new IllegalStateException("Cannot load "
        + conventionalName, ex);
        }
        }
    &lt;/class&gt;&lt;/pre&gt;
The init will load the ResourceBundle and the fxml-file with an instance of the class FXMLLoader. The most importand step is the setting of the ControllerFactory. Inside the instance of the ControllerFactory you will see the methodcall&lt;b&gt; InjectionProvider.instantiatePresenter(p);&lt;/b&gt;&amp;nbsp; This ist the place where the injection will be taken place. One big point to know is, only inside a controller/presenter you will be able to use injection. Inside the Presenter no injection is available.&lt;br /&gt;
&lt;br /&gt;
&lt;b&gt;The InjectionProvider - DI with reflection&lt;/b&gt;&lt;br /&gt;
The InjectionProvider ist the heart of the framework. The base steps are the following:&lt;br /&gt;
- create an instance&lt;br /&gt;
- inject the attributes with the annotation Inject&lt;br /&gt;
- call the method with annotation Postconstruct&lt;br /&gt;
Thats all... but how it is realized?&lt;br /&gt;
&lt;br /&gt;
The first step is quite easy, just call &lt;b&gt;clazz.newInstance()&lt;/b&gt;.&lt;br /&gt;
&lt;br /&gt;
Step two is a littele bit more complex. You have to instantiate the attributes but the the attributes inside too. Thes means the injection must be done recursive. To do this you will check the attributes if they are annotated with Inject, if so, do the same for this instance.. and so on..&lt;br /&gt;
There is a small thig to know. The implementation from Adam will only create one instance of every used class. This means you will get &lt;b&gt;only singletons!!&lt;/b&gt;&amp;nbsp;And this for the complete application.&lt;br /&gt;
&lt;br /&gt;
The last step is easy again, call all methods with the annotation Postconstruct per reflection.&lt;br /&gt;
&lt;br /&gt;
&lt;pre class="brush: java"&gt;static Object instantiateModel(Class clazz) {
    Object product = models.get(clazz);
    if (product == null) {
        try {
            product = injectAndInitialize(clazz.newInstance());
            models.put(clazz, product);
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalStateException(
                "Cannot instantiate view: " + clazz, ex);
        }
    }
    return product;
}

static Object injectAndInitialize(Object product) {
    injectMembers(product);
    initialize(product);
    return product;
}

static void injectMembers(final Object instance) {
    Class aClass = instance.getClass();
    Field[] fields = aClass.getDeclaredFields();
    for (final Field field : fields) {
        if (field.isAnnotationPresent(Inject.class)) {
            Class type = field.getType();
            final Object target = instantiateModel(type);
            AccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public Object run() {
                    boolean wasAccessible = field.isAccessible();
                    try {
                        field.setAccessible(true);
                        field.set(instance, target);
                        return null; // return nothing...
                    } catch (IllegalArgumentException | 
                                IllegalAccessException ex) {
                        throw new IllegalStateException(
                            "Cannot set field: " + field, ex);
                    } finally {
                        field.setAccessible(wasAccessible);
                    }
                }
            });
        }
    }
}
static void initialize(Object instance) {
    invokeMethodWithAnnotation(instance, PostConstruct.class);
}
&lt;/pre&gt;
&lt;b&gt;Lesson Learned&lt;/b&gt;&lt;br /&gt;
The framework afterburner.fx from Adam Bien is really small without any configuration. You could use this to inject Instances per annotation Inject. If you want to use this inside your application you have to know the following:&lt;br /&gt;
&lt;br /&gt;
&lt;br /&gt;
&lt;ul&gt;
    &lt;li&gt;There are no Scopes, all instances will have the the lifecycle of the application. The only way to terminate them earlier is to call the method forgettAll(), but this will terminate all instances. The method annotated with PreDestroy will be called before. You could not select the order the instances are destroyed.&lt;/li&gt;
    &lt;li&gt;All instances are singletons&lt;/li&gt;
    &lt;li&gt;No Producers, this means you could not abstract over an Interface layer. Or you can not switch between different implementations like you could do with Qualifiers.&lt;/li&gt;
&lt;/ul&gt;
&lt;div&gt;
    If you could deal with this limitations, this will be good form you. But to use this to learn more about injection works, this is a good project to play with.&lt;/div&gt;
&lt;br /&gt;
&lt;br /&gt;
&lt;br /&gt;
&lt;br /&gt;
&lt;br /&gt;
&lt;br /&gt;&lt;/div&gt;
							&lt;/div&gt;
							&lt;footer class="article-footer clearfix"&gt;
								&lt;span class="post-author"&gt;&lt;a href="/team/sven-ruppert" rel="author"&gt;Sven Ruppert&lt;/a&gt;&amp;nbsp;&amp;nbsp;|&amp;nbsp;&amp;nbsp;&lt;/span&gt;
								&lt;span class="post-date"&gt;&lt;a href="#" rel="date"&gt;2013-12-09&lt;/a&gt;&amp;nbsp;&amp;nbsp;|&amp;nbsp;&amp;nbsp;&lt;/span&gt;
							&lt;/footer&gt;
						&lt;/article&gt;</content>
        <summary>Das ist der Inhalt der in Kurzform angezeigt werden soll</summary>
    </entry>
</feed>
