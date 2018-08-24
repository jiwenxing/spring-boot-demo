如果你希望console里的日志能够按照不同的级别有不同的着色，如下图这样便于查看错误和异常日志，有一个很好用的eclipse插件`Grep console`可以帮你实现。

![](http://7xry05.com1.z0.glb.clouddn.com/201707142005_208.png)

首先安装Grep console插件，在help页签eclipse market中搜索Grep console并安装（完成后需要重启eclipse），然后在preferences中进行设置如下图，一定注意将expression由`.*(\Q[FATAL]\E).*`改为`.*(\QFATAL\E).*`，要不然不会生效。

![](http://7xry05.com1.z0.glb.clouddn.com/201707142027_598.png)

另外可以在windows页签view中选择将grep view窗口展示出来，可以将一些日志过滤到grep view中，便于查看。

![](http://7xry05.com1.z0.glb.clouddn.com/201707142031_586.png)

关于grep console的更多设置及其含义可以参考以下链接：

- [Add color to eclipse console output using the log level](https://atechblogagain.wordpress.com/2013/06/03/add-color-to-eclipse-console-output-using-the-log-level/)
- [Grep Console - User Guide](http://marian.schedenig.name/wp-content/static/grepconsole_userguide/)