<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>sse测试</title>
  <body>
    <div id="msg_from_server"></div>
    <script type="text/javascript" src="/js/jquery-1.11.1.min.js"></script>
    <script type="text/javascript">
    if (!!window.EventSource) {  //检测浏览器是否支持SSE
       var source = new EventSource('push'); //为http://localhost:8080/push
       s='';
       source.addEventListener('message', function(e) {

           s=e.data+"<br/>"
           console.log(e.data)
           $("#msg_from_server").html(s);

       });

       source.addEventListener('open', function(e) {
            console.log("连接打开.");
       }, false);

       source.addEventListener('error', function(e) {
            if (e.readyState == EventSource.CLOSED) {
               console.log("连接关闭");
            } else {
                console.log(e.readyState);    
            }
       }, false);
     } else {
            console.log("没有sse");
     } 
</script>
  </body>
</html>