# **Symbolic fitting of SCOTSv2.0 BDD controllers**

**Author:** [Dr. Ivan S. Zapreev](https://nl.linkedin.com/in/zapreevis)

**Project pages:** [Git-Hub-Project](https://github.com/ivan-zapreev/SCOTS2SR)

## **Introduction**

This software is a Java-based UI tool for symbolic fitting of the Binary Decision Diagram (BDD) based controllers as provided by `SCOTSv2.0` (<https://gitlab.lrz.de/matthias/SCOTSv0.2>).

Our software uses Symbolic Regression, powered by Genetic Programming and realized by the `SR2JLIB` library (<https://github.com/ivan-zapreev/SR2JLIB>), to fit the controller's data with functions. As a result, a deterministic vector function is generated representing the control law determinization of the original, possibly non-deterministic, BDD controller.

This software is useful in generating functional controllers for embedded platforms which do not allow for importing BDD libraries (in our case it is `CUDD` <http://davidkebo.com/cudd>). Also, by using functional representation the memory required for storing the control law can be drastically reduced, even compared to that encoded as a BDD.

As the symbolic regression rarely provides a `100%` fit, the domain of the resulting controller can be somewhat smaller than that of the original one. The way of re-computing the domain of the functional controller will be described in this document as well.

## **Dependencies**

This project is dependent on:

1. Java Symbolic Regression Library - `SR2JLIB` (<https://github.com/ivan-zapreev/SR2JLIB>)
2. JNI interface for the fitness-computing backend - `SCOTS2JNI` (<https://github.com/ivan-zapreev/SCOTS2JNI>)
2. Fitness computing backend itself (indirectly by loading of its dynamic library) - `SCOTS2DLL` (<https://github.com/ivan-zapreev/SCOTS2DLL>)

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

1. Build `SR2JLIB` following the instructions in <https://github.com/ivan-zapreev/SR2JLIB>
2. Build `SCOTS2JNI` following the instructions in <https://github.com/ivan-zapreev/SCOTS2JNI>
3. Build `SCOTS2DLL` following the instructions in <https://github.com/ivan-zapreev/SCOTS2DLL>

Further one requires to

1. Open the `SCOTS2SR ` project in Netbeans
2. Initiate the project `build` from the Netbeans IDE
3. Run the project from within the Netbeans IDE

## **Tool's configuration**
The initial start up ...
Where the configuration data is stored ...

## **Tool's interface**

## **Loading controllers**

## **Monitoring logs**

## **Fitting controllers**

## **Exporting controller**

## **Using controllers**
