# Wrappy
Wrappy is an annotation-processor library which help you to wrap existing API.  
You can easily create custom wrapper to add tracking log, create asynchronous methods ...

## Installation

You need to include Wrappy in your dependency list.
```groovy
dependencies {
    compileOnly "me.dfournier.wrappy:wrappy-annotations:a.b.c"
    annotationProcessor "me.dfournier.wrappy:wrappy-compiler:a.b.c"
}
```

To include Wrappy extensions, just add it to your dependency list.
```groovy
dependencies {
    annotationProcessor "me.dfournier.wrappy:wrappy-rxjava2:a.b.c"
}
```

## Usage
To create a wrapper, declare a function which takes only **1** parameter, the wrapped class and returns the wrapper class.  
It will generate automatically the expected class. You can then return a new instance of the generated class (The primary constructor usually only takes the wrapped class in parameter but a more complex generator may need additional data).  
Finally, to trigger the annotation processing, you need to annotate the function with the `@Wrappy` annotation. The only value it accepts is the processor name.

The wrapped class will contain an equivalent of all the public method existing in the wrapped class. 

## Example
The wrapped class is:
```java
public final class WrappedClass {
    public void printName(String name) {
        // NOOP
    }
}
```

A generated class can be generated with the following snippet:
```java
public final class MyModule {
    @Wrappy(processor = "Empty")
    public WrapperClass getWrapper(WrappedClass wrappedClass) {
        return new WrapperClass(wrappedClass);
    }
}
```

## Known wrapper
Name | Behavior | Dependency | Comment
-- | -- | -- | --
Empty | Generate a wrapper class which simply calls the wrapped class | Included by default | 
RxJava2 | Generate a wrapper class transform all API in a Single / Completable Stream | me.dfournier.wrappy:wrappy-rxjava2 | 

## TODO
- Update files with the Licence header
- Update Unit Test, especially for the Generator part
- Add "AsyncTask Wrapper"
- Add "Logging wrapper"