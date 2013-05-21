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
* Dynamic shadow mapping

The included demo Activity shows a simple scene illuminated by a single light source with dynamic shadows.
Shadow mapping is very experimental and it took some effort to get this to work on my Galaxy Nexus since it
does not support depth textures. The current code will only work for this demo scene.

Screenshots:

![Shadow Mapping 1](/docs/images/shadow1.png)

![Shadow Mapping 1](/docs/images/shadow2.png)

![Texture Mapping 1](/docs/images/texture1.png)

![Texture Mapping 1](/docs/images/texture2.png)
