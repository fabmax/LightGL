LightGL
=======

A very light-weight pure-Java Android OpenGL framework.

The main purpose of this is for me to learn OpenGL ES 2.0, but I aim for a graphics engine that is actually usable.
However there's a long way to go.

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

![Shadow Mapping 1](/docs/images/blocks1.png)

![Shadow Mapping 1](/docs/images/blocks2.png)
