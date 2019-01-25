precision mediump float;

uniform vec3 u_Color;
uniform vec3 u_CameraPosition;
uniform samplerCube u_CubeMapUnit;

varying vec3 v_Position;
varying vec3 v_Normal;

void main() {
    vec3 normal = normalize(v_Normal);
    vec3 eyeDirection = v_Position - u_CameraPosition;
    vec3 reflected = normalize(reflect(eyeDirection, normal));
    reflected.z = -reflected.z;
    vec4 texColor = textureCube(u_CubeMapUnit, reflected);
    gl_FragColor = vec4(
        u_Color.r * texColor.r,
        u_Color.g * texColor.g,
        u_Color.b * texColor.b,
        1.0);
}
