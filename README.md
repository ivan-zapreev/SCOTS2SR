# **Symbolic fitting of SCOTSv2.0 BDD controllers**

**Author:** [Dr. Ivan S. Zapreev](https://nl.linkedin.com/in/zapreevis)

**Project pages:** [Git-Hub-Project](https://github.com/ivan-zapreev/SCOTS2SR)

## **Introduction**

This software is a Java-based UI tool for symbolic fitting of the [BDD-based](https://en.wikipedia.org/wiki/Binary_decision_diagram) controllers as produced by `SCOTSv2.0` - a tool to compute [discrete abstractions and symbolic controllers](<https://gitlab.lrz.de/matthias/SCOTSv0.2>).

Our software uses Symbolic Regression, powered by Genetic Programming and realized by the [SR2JLIB library](https://github.com/ivan-zapreev/SR2JLIB), to fit the controller's data with functions. As a result, a deterministic vector function is generated representing the control law determinization of the original, possibly non-deterministic, BDD-based controller.

This software is useful in generating functional controllers for embedded platforms which do not allow for importing BDD libraries (in our case the [CUDD library](http://davidkebo.com/cudd). Since, by using functional representation, memory required for storing a control law can be drastically reduced, this tool is handy for platforms having limited memory resources.

Symbolic regression is known to easily provide a very good fit of data, whereas it is very hard for it to get an absolute (`100%`) fit. This is caused by the random nature of the process. Therefore, the found functional controller will most likely not be completely fit meaning that its actual domain will be somewhat smaller than that of the original one. This is however not an issue at all. The functional controller's domain can be recomputed and the way of doing this is described in the upcoming sections of the document.

## **Dependencies**

This project is dependent on:

1. Java Symbolic Regression Library - [SR2JLIB](https://github.com/ivan-zapreev/SR2JLIB)
2. JNI interface for the fitness-computing backend - [SCOTS2JNI](https://github.com/ivan-zapreev/SCOTS2JNI)
2. Fitness computing backend itself (indirectly by loading of its dynamic library) - [SCOTS2DLL](https://github.com/ivan-zapreev/SCOTS2DLL)

## **Required tools**

In order to build the project one requires to have:

1. Netbeans version 8.2 or later in its version containing: Java/JDK and C++
2. JDK version 1.8 or later

## **Build instructions**

Before the project can be build `SR2JLIB`, `SCOTS2JNI`, and `SCOTS2DLL ` are to be downloaded and build in the folders next the the folder containing this project.

The directory structure is assumed to be as follows:

```
$ ls -al
drwxr-xr-x  10 user  staff   320 May 15 10:22 .
drwxr-xr-x   8 user  staff   256 Feb 20 08:41 ..
drwxr-xr-x  12 user  staff   384 May 15 15:36 SR2JLIB
drwxr-xr-x  13 user  staff   416 May 15 16:14 SCOTS2DLL
drwxr-xr-x   8 user  staff   256 May  7 12:12 SCOTS2JNI
drwxr-xr-x  13 user  staff   416 May 15 16:15 SCOTS2SR
...
```
Where `SCOTS2SR` is storing this project. Further one needs to:

1. Build `SR2JLIB` following [the instructions](https://github.com/ivan-zapreev/SR2JLIB)
2. Build `SCOTS2JNI` following [the instructions](https://github.com/ivan-zapreev/SCOTS2JNI)
3. Build `SCOTS2DLL` following [the instructions](https://github.com/ivan-zapreev/SCOTS2DLL)

Further one requires to

1. Open the `SCOTS2SR ` project in Netbeans
2. Initiate the project `build` from the Netbeans IDE
3. Run the project from within the Netbeans IDE

## **Tool's configuration**
The tool's configuration properties are stores in the `config.properties` file located in the project folder. These are updated each time the tool exits.

In order to re-set the properties to the *default* ones it suffices to delete the `config.properties` file.

Each time the tool is started it loads the dynamic native library produced by `SCOTS2DLL`. If the library can not be found or can not be loaded an error message is dislayed, followed by the *File Open Dialog* in which the user is supposed to select the location of the `SCOTS2DLL`'s project dynamic library. The details on why the library could not be loaded, can be found in the Netbeans console log. The proper library location is stored in `config.properties` along with other parameters. Therefore typically, choosing the proper library is needed only the first time the tool is started.

## **Tool's interface**
The main tool's interface is depicted in the figure below:

![The main tool's UI](./doc/img/tool_ui.png)

### The top panel

![The tool's top](./doc/img/tool_ui_top.png)

Contains the main control buttons for:

*  *Load* - to load the BDD controller
*  *Run* - run the symbolic regression for the loaded controller
*  *Stop* - stop the symbolic regression run
*  *Save* - export the generated functional controller

This panel is also used for displaying progress bars, when needed.

### The bottom panel

![The tool's bottom](./doc/img/tool_ui_bottom.png)

Contains the maun UI log which allow one to monitore the tool's status. Advanced status details can be obtained by monitoring the log files. The latter is explained in the subsequent sections.

### The left panel
Is used for configuration options and is split into two tabs.

![The tool's left - options](./doc/img/tool_ui_left_opt.png)

The first one contains various tool options, including those that influence the symbolic regression.

![The tool's left - grammar](./doc/img/tool_ui_left_gr.png)

The second one is fully devoted to the grammar to be used when generating the controllers. For the ldetails on the possibilities for the grammar please refer to [SR2JLIB](https://github.com/ivan-zapreev/SR2JLIB/).

### The right panel

![The tool's right](./doc/img/tool_ui_right.png)

Is devoted to monitoring the population grid and fitness values, for both *actual* and *extended fitness*. If the latter is devoted then the interface adjusts by hiding the *extended fitness* related UI component.

![The tool's right, adjusted](./doc/img/tool_ui_right_adj.png)

## **Loading controllers**

## **Monitoring logs**

## **Fitting controllers**

## **Exporting controller**
The resulted functional controller, along with the unfit domain points, is exported into respectively text and BDD files...

## **Using controllers**

## **Frequentry Asked Questions**
Below you will find the list of the frequently asked questions with our answers to them:

> Can I use the tool on Windows platforms?

* That would be possible if CUDD would be portable to Windows. For now we provide no support for Windows platforms, not even under [Cygwin](https://www.cygwin.com/).

> What does it mean when the average fitness is above the maximum one?

* This is caused by numerical errors in mean computations. Nothing to worry about, this might be fixed in the future releases. For now it is important to remember that the main fitness indicator is the maximum of the *actual fitness* value.

> When do I stop my symbolic regression run?

* Unless the maximum iterations count is set to a concrete value the regression will run forwever, unless a 100% individual is found. The latter is a rare occasion so in principle one can stop symbolic regression at any time. The less fit is the functional controller the smaller will be the domain it is valid for. However the rule of thumb is iterate until the average fitness values flatten out and stop growing. This indicates the convergence of regression the some local maximum.

> How is it possible that the actual fitness can drop?

* In general if there is just *actual fitness* enabled (no *extended fitness* is used) this should not be happenings, unless it is a software bug. However, if *extended fitness* is enabled then individuals are compared based on it and this can cause an individual having higher *extended fitness* to remove an individual with a higher *actual fitness* from the grid. Typically the situation is restored and the *actual fitness* values are re-gained as the growing *extended fitness* pulls the *actual fitness* up over time.

