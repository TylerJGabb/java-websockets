# java-websockets
learning websockets from the ground up

This is a project that I made to try and learn about websockets https://tools.ietf.org/html/rfc6455 and attempt to write a libray that 
can be used on top of vertx to facilitate the movement of messages througout the **APLICATION LAYER** of several applications
connected via web-sockets. When a data frame arrives It will be resolved into a message that then is given to a handler that defines
how the application should behave when said mesage is received. 

Like almost every other one of my repos, this is a sandbox... hopefully some day I'll actually make something! 
