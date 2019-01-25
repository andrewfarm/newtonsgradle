precision mediump float;

const vec3 LIGHT_DIRECTION = vec3(1.0, 1.0, 1.0);

const float SPECULAR_STRENGTH = 1.0;
const float SPECULAR_POWER = 20.0;

uniform vec3 u_Color;
uniform vec3 u_CameraPosition;

varying vec3 v_Position;
varying vec3 v_Normal;

void main() {
    vec3 normal = normalize(v_Normal);

    //directional lighting
    float brightness = max(dot(normal, normalize(LIGHT_DIRECTION)), 0.0);

    //specular lighting
    vec3 reflected = normalize(reflect(-LIGHT_DIRECTION, normal));
    vec3 lookVector = normalize(u_CameraPosition - v_Position);
    float specular = max(dot(reflected, lookVector), 0.0);
    float specularBrightness = SPECULAR_STRENGTH * pow(specular, SPECULAR_POWER);
    vec3 specularLight = vec3(specularBrightness);

    gl_FragColor = vec4(brightness * u_Color + specularLight, 1.0);
}
