# The butler did it!



This is a library and a set tools aimed to find performance issues derived from JVM monitors locks. By taking the output yield by

```
jstack -l <PID> 
```

parsing it and generating useful representations of the lock dependencies between threads. 

- **As a multiplatform library:** It offers an ADT for threads https://github.com/pfcoperez/thebutlerdidit/tree/master/shared/src/main/scala/org/pfcoperez/thebutlerdidit/model :
https://github.com/pfcoperez/thebutlerdidit/blob/7ac830aac6b083c64dbde2f67aef88c524053260/jvm/src/main/scala/org/pfcoperez/thebutlerdidit/ProcessInput.scala#L3-L11

- **As a JVM application:** It takes the output 

And making it more readable as thei a [Graphviz DOT language](https://graphviz.org/doc/info/lang.html) representation () of the lock dependencies between threads


## Features

- Browser app:



- Library:

- [x] aa 
- [x] bb
- [x] cc

- [ ] dd
- [ ] ee
- [ ] ff
- [ ] gg

- 