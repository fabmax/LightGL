LightGL
=======

A very light-weight pure-Java Android OpenGL framework.

I made this mainly to play a little with OpenGL ES 2.0 on Android, but I aim for a graphics engine
that is actually usable. However there's a long way to go.

I built a cool live wallpaper with this, 
[available on Google Play](https://play.google.com/store/apps/details?id=de.fabmax.blox.pro)!

Features:
* NEW: (Very basic) Integration of JBullet physics engine
* Arbitrary triangle meshes with texture mapping or vertex colors
* Model loading for .obj files
* Simple scene graph supporting arbitrary object transformations
* Various custom shaders
* Dynamic shadow mapping without usage of any OpenGL extensions

The included demo App currently has two demo scenes: The first scene loads an .obj model and renders
it with dynamic shadows. The second scene gives a little physics demo; however, performance is not
that great.

Dynamic shadow mapping currently uses a standard texture instead of the usual depth texture, because
depth textures are not supported on many GL ES 2.0 devices. OpenGL ES 3.0 fixes that but is not
supported yet.

Screenshots:

![Physics Simulation](/docs/images/physics.png)

![Shadow Mapping 1](/docs/images/texture1.png)

![Shadow Mapping 1](/docs/images/texture2.png)




    Copyright 2013 Max Thiele
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
