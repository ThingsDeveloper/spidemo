# spidemo
该项目演示了SPI接口的基本使用

##  硬件准备
在运行该项目之前，我们要准备如下元器件：
* 面包板 1块
* Max7219点阵模块
* 杜邦线（公对母）若干

>广告时间咯：如果你还没有自己的开发板和元器件，到我们的“1024工场微店”来逛逛一逛吧（文章底部二维码），这里能一次性有买到你想要的！

## 电路图
![电路图](img/circuit.png)

## 构建运行
1. 在Android Studio中，点击Run即可。
2. 如果你通过gradle命令进行构建，则：
```bash
gradle installDebug
adb shell am start com.chengxiang.spidemo/.MainActivity
```

## 运行效果
运行程序，显示器上闪烁显示1,2,3数字如下：

![结果1](img/result1.jpg)

![结果2](img/result2.jpg)

* * * * *
*1.新技术，新未来！欢迎大家关注“1024工场”微信服务号，时刻关注我们的最新的技术讯息。*

![服务号](img/fuwuhao.jpg)     

*2.抛弃各种找元器件的烦恼，来“1024工场”微店，一次性买到你所想要的。*

![微店](img/weidian.jpg) 

*3.加入“Android Things开发”QQ讨论群，一起学习一起Hi。（甭客气！尽情的扫描或者长按！）*

![qq群](img/qq.png)         