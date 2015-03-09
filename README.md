VoxelTerrain
============

Voxel Engine for jMonkeyEngine

Based on [dual contour](http://www1.cse.wustl.edu/~taoju/research/dualContour.pdf). 
This is a grid based implementation of dual contour. 

Instead of using QR decomposition for calculating isopoint in cube I used Leonardo Augusto Schmitz iterative particle based minimization. My implementation is [here](https://github.com/TheWiseLion/VoxelTerrain/blob/master/src/main/java/voxelsystem/surfaceextractors/ExtractorUtils.java). Details for the algorithm is [here](http://www.inf.ufrgs.br/~comba/papers/thesis/diss-leonardo.pdf) in section 4.2.1 and 4.2.2



Developed: Summer of 2014. 

Language: Java

![alt tag](http://i.imgur.com/fclhOdN.png)
![alt tag](http://i.imgur.com/nc5D4a5.png)
![alt tag](http://i.imgur.com/CDnlpYy.png)
![alt tag](http://i.imgur.com/RVYxhpv.png)
![alt tag](http://i.imgur.com/urhaQAb.png)
![alt tag](http://i.imgur.com/qDvsWL2.png)

Low Quality Video:
[![Alt text for your video](http://img.youtube.com/vi/RKyqeR4NuGo/0.jpg)](http://www.youtube.com/watch?v=RKyqeR4NuGo)






