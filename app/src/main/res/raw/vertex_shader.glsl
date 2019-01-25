uniform mat4 u_MvpMatrix;
uniform mat4 u_ModelRotationMatrix;

attribute vec3 a_Position;
attribute vec3 a_Normal;

varying vec3 v_Position;
varying vec3 v_Normal;

void main() {
    vec4 transformedPos = u_MvpMatrix * vec4(a_Position, 1.0);
    v_Position = a_Position;
    v_Normal = (u_ModelRotationMatrix * vec4(a_Normal, 1.0)).xyz;

    gl_Position = transformedPos;
    gl_PointSize = 10.0;
}
