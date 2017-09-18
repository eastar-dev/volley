## What different android.log.Log?

android.log.Log is more smart, powerfull then android.util.Log
Android developers often need to logcat.
So I made it.

### log pretty output at JSON, XML another .... ?
android.log.Log help u'r project tracking find bug.

### log quick setting u'r project
just replace all 'import android.util.Log;' -> 'import android.log.Log;' that it!





## How...

### Gradle with jitpack

#### Add it in your root build.gradle at the end of repositories:
```javascript

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

```
#### Add the dependency
```javascript

	dependencies {
	        compile 'com.github.djrain:log:v0.1'
	}


```


