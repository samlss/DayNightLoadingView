# DayNightLoadingView
A day night loading view(一个日夜切换的loading view).


[![Api reqeust](https://img.shields.io/badge/api-19+-green.svg)](https://github.com/samlss/SignalLoadingView)  [![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://github.com/samlss/DayNightLoadingView/blob/master/LICENSE) [![Blog](https://img.shields.io/badge/samlss-blog-orange.svg)](https://blog.csdn.net/Samlss)

<br>

  * [中文](#%E4%B8%AD%E6%96%87)
  * [English](#english)
  * [License](#license)

<br>

![gif1](https://github.com/samlss/DayNightLoadingView/blob/master/screenshots/screenshot1.gif)


---

## 中文

### 使用<br>
在根目录的build.gradle添加这一句代码：
```
allprojects {
    repositories {
        //...
        maven { url 'https://jitpack.io' }
    }
}
```

在app目录下的build.gradle添加依赖使用：
```
dependencies {
    implementation 'com.github.samlss:DayNightLoadingView:1.0'
}
```

布局中使用：
```
<com.iigo.library.DayNightLoadingView
        android:id="@+id/dn_loading"
        app:background_color="#66000000"
        app:moon_color="#D0D2DF"
        app:star_color="#F3EBB8"
        app:sun_color="#FFF600"
        app:sunshine_color="#DB261B"
        android:layout_width="400dp"
        android:layout_height="300dp"/>
```

<br>

代码中使用：
```
  dayNightLoadingView.resume(); //恢复动画
  dayNightLoadingView.pause(); //暂停动画
  
  dayNightLoadingView.stop(); //停止和释放动画，可在activity销毁时调用
```

<br>

属性说明：

| 属性        | 说明           |
| ------------- |:-------------:|
| background_color      | 背景颜色 |
| sun_color | 太阳颜色 |
| sunshine_color | 阳光颜色 |
| moon_color | 月亮颜色 |
| star_color | 星星颜色 |

<br>

由于使用了Animator和AnimaterSet的resume和pause方法，因此api要求为 >= 19，如果你想兼容更低的api，
方法有：
 - 不调用该库的resume和pause接口,最低可兼容到api12
 - 你自己修改代码
 - 联系我，等我有时间的时候尽量完善一下 

<br>

---

## English


### Use<br>
Add it in your root build.gradle at the end of repositories：
```
allprojects {
    repositories {
        //...
        maven { url 'https://jitpack.io' }
    }
}
```

Add it in your app build.gradle at the end of repositories:
```
dependencies {
    implementation 'com.github.samlss:DayNightLoadingView:1.0'
}
```


in layout.xml：
```
<com.iigo.library.DayNightLoadingView
        android:id="@+id/dn_loading"
        app:background_color="#66000000"
        app:moon_color="#D0D2DF"
        app:star_color="#F3EBB8"
        app:sun_color="#FFF600"
        app:sunshine_color="#DB261B"
        android:layout_width="400dp"
        android:layout_height="300dp"/>
```

<br>

in java code：
```
  dayNightLoadingView.resume(); //resume animator
  dayNightLoadingView.pause(); //pause animator
  
  dayNightLoadingView.stop(); //Stop and release the animation, can be called when the activity is destroyed
```
<br>


Attributes description：

| attr        | description  |
| ------------- |:-------------:|
| background_color      | the background color |
| sun_color | the sun color |
| sunshine_color | the sunshine color |
| moon_color | the moon color |
| star_color | the stars color |

<br>

Due to use of the resume and pause methods of Animator and AnimalSet, the api requirement is >= 19, 
if you want  to be compatible with lower apis,
the methods are:
- Do not call the resume and pause method of this library, the minimum compatible with api 12.
- Modify the code by yourself.
- Contact me, i will try to improve it when I have time.

[id]: http://example.com/ "Optional Title Here"

---

## [LICENSE](https://github.com/samlss/DayNightLoadingView/blob/master/LICENSE)
