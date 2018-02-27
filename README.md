I applied to Hazelcast on or about 2/16/2018. There was a code
challenge to complete as part of their process. So, I decided
to upload my code even though I didnt wind up getting hired.

Here are the instructions which are vague if you ask me:

"Imagine an environment that consists of 10 nodes. Each node is a separate JVM process and could potentially be running on a distinct physical machine.

Your task is to write an application that will run on all of the 10 nodes. Some nodes may start seconds or minutes later than others. Some may not be started at all. The application should coordinate between the nodes so that System.out.println("We are started!") is only called by one of them.
There is no need to build a distributed system from scratch. You can use an existing library for the solution."

Justin said that he team didnt like my approach/understanding of concurrency.
Well, I thought that any puts to the IMap were atomic. I guess that in recent
versions of Hazelcast, thee is now a way to wrap a transaction around the
IMap put using something like this

map.thaw().put( "key", "value" ).freeze();

I think this is the stackoverflow thread:

https://stackoverflow.com/questions/17182831/hazelcast-map-synchronization

In older versions I believe atomicity was implicit by default.
