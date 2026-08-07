### Sum type

```kotlin
//1.
sealed interface Out {

data class Error(error:Throwable, msg:String):Out
data class Success<T>(val data:T):Out
}

2.
sealed interface ImageSource {
   data class Network(val url:Url):ImageSource
   data class Resources(val resId: Int):ImageSource
   data class Raw(val image:String):ImageSource
}
```

### Product type

```kotlin
//1. 
data class Geo(val lat:Double, val lng:Double) {

}

//2.
data class Pair<A, B>(val first: A, val second: B) {
   
}
```
