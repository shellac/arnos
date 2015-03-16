# Introduction #

This document is intended to help users download, install and configure the arnos maven web project.

# Requirements #
  * Java 1.5+
  * Servlet 2.5 container (e.g. Tomcat 6+)
  * Maven (for installation from source)

# Installation (from source) #
Obtain source code
```
hg clone https://arnos.googlecode.com/hg/ arnos 
```

Generate the war file
```
cd arnos
mvn install
```

Finally, copy/deploy the war file to your servlet container and your good to go.
```
cp arnos/arnos.war [tomcat_root_path]/webapps
```

# Configuration #

Before you can use arnos, you'll need to add a 'project'. A project is simply a collection of endpoints. Projects can be set up via JMX. For this to work, you need to make sure you've started your servlet container with the following commandline parameter: `com.sun.management.jmxremote.port=portNumber`. Then you can fire up jconsole, connect to this process and navigate to `MBeans->org.wf.arnos.controller.model->ProjectsManager->org.wf.arnos.controller.model.ProjectsManager#0->Operations`. From here you can create a new project, list current ones or remove a project from the system.

Once a project as been created, you can add/remove endpoints either via the API, or using the test page: `http://[tomcat_url:port]/arnos/test.html`

Changes to projects & endpoints are serialised to an xml file (default is `[tomcat_root_path]/webapps/arnos/WEB-INF/db_persistant.xml`. If you prefer, you can make all modifications here and restart arnos.
e.g.
```
<list>
  <project>
    <name>testproject</name>
    <endpoints>
      <endpoint>
        <location>http://localhost:9099/books/1</location>
        <id>966249ebcdc8fdda81f41d8f266725acbfa5cffa</id>
      </endpoint>
    </endpoints>
    ...
  </project>
</list>
```

By default this file lives in the project's folder. If you wish to maintain project configuration persistence between redeploys, you'll need to create a config file. Place the following in `[tomcat_root_path]/arnos.properties`

```
db.file=../arnosdb.xml
```

This will force arnos to place the xml serialised form of the project configuration into `[tomcat_root_path]/arnosdb.xml`. An optional cache file property can also be used if you want to override the default ehcache.xml file.

```
cache.file=../ehcache.xml
```