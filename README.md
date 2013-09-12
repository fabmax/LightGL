LightGL
=======

A very light-weight pure-Java Android OpenGL framework.

I made this mainly to play a little with OpenGL ES 2.0 on Android, but I aim for a graphics engine
that is actually usable. However there's a long way to go.

I built a cool live wallpaper with this, 
[available on Google Play](https://play.google.com/store/apps/details?id=de.fabmax.blox.pro)!

Features:
* Arbitrary triangle meshes with texture mapping or vertex colors
* Model loading for .obj files
* Simple scene graph supporting arbitrary object transformations
* Various custom shaders
* Dynamic shadow mapping without usage of any OpenGL extensions

The included demo Activity shows an animated scene illuminated by a single light source with dynamic shadows.
Shadow mapping works pretty well now. Just set a ShadowRenderPass as pre-pass in the engine, use a ShadowShader
to render your Mesh and set the scene bounds for the ShadowRenderPass with setSceneBounds(BoundingBox)... and BOOM
dynamic shadows :)

The depth rendering during the pre-pass uses a standard texture instead of the usual depth texture, because depth textures
are not supported on many GL ES devices.

Screenshots:

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
