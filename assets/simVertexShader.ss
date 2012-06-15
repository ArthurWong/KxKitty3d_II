#define NUM_TEXTURES				2
#define GLI_FOG_MODE_LINEAR			0
#define GLI_FOG_MODE_EXP			1
#define GLI_FOG_MODE_EXP2			2

struct Light {
	vec4	position;	//light position for a point / spot light or normalized dir. for a directional light
	vec4	ambient_color;
	vec4	diffuse_color;
	vec4	specular_color;
};

const float 	c_zero 	= 0.0;
const float		c_one	= 1.0;
const int		indx_zero = 0;
const int		indx_one  = 1;

uniform	mat4	mvp_matrix;		//combined model-view-projection matrix
uniform mat4	modelview_matrix;	//model-view matrix
uniform mat3	inv_modelview_matrix;	//inverse model-view matrix used

//to transform normal
uniform int	enable_tex[NUM_TEXTURES];		//texture enables

uniform Light		light_state[8];
uniform int		light_enable_state[8];		//indicate which light is enabled

uniform int		num_lights;
uniform int		enable_lighting;				//is lighting enabled

uniform vec3 uCamera;							//position of Camera

//********************************************
// vertex attributes - not all of them may be passed in
//********************************************

attribute vec4	a_position;
attribute vec4	a_texCoord0;
attribute vec4	a_texCoord1;
attribute vec4	a_color;
attribute vec3	a_normal;

//*******************************************
// varying variables output by the vertex shader
//*******************************************

varying	vec4	v_texcoord[NUM_TEXTURES];
varying vec4	v_front_color;
varying vec4	v_back_color;
varying float	v_fog_factor;
varying float	v_ucp_factor;

varying vec4 vAmbient;
varying vec4 vDiffuse;
varying vec4 vSpecular;

//*********************************************
// temporary variables used by the vertex shader
//*********************************************
vec3		n;

//��λ����ռ���ķ���
void pointLight(				//��λ����ռ���ķ���
  in vec3 normal,				//������
  inout vec4 ambient,			//����������ǿ��
  inout vec4 diffuse,			//ɢ�������ǿ��
  inout vec4 specular,			//���������ǿ��
  in vec3 lightLocation,		//��Դλ��
  in vec4 lightAmbient,			//������ǿ��
  in vec4 lightDiffuse,			//ɢ���ǿ��
  in vec4 lightSpecular			//�����ǿ��
)
{
  //���㻷����ǿ��
  ambient = lightAmbient;						//ֱ�ӵó������������ǿ��  
  
  //����ɢ���ǿ��
  vec3 normalTarget = aPosition+normal;			//����任��ķ�����
  vec3 newNormal = (uMMatrix * vec4(normalTarget, 1)).xyz - (uMMatrix * a_position).xyz;
  newNormal = normalize(newNormal); 			//�Է��������
  //����ӱ���㵽�����������
  vec3 eye = normalize(uCamera - (uMMatrix * a_position).xyz);  
  //����ӱ���㵽��Դλ�õ�����vp
  vec3 vp = normalize(lightLocation - (uMMatrix * a_position).xyz);  
  vp = normalize(vp);//��ʽ��vp
  vec3 halfVector = normalize(vp + eye);		//����������ߵİ�����    
  float shininess = 50.0;						//�ֲڶȣ�ԽСԽ�⻬
  float nDotViewPosition = max(0.0, dot(newNormal, vp)); 	//��������vp�ĵ����0�����ֵ
  diffuse = lightDiffuse * nDotViewPosition;				//����ɢ��������ǿ��
  
  //���㷴���ǿ��
  float nDotViewHalfVector = dot(newNormal, halfVector);	//������������ĵ�� 
  float powerFactor = max(0.0, pow(nDotViewHalfVector, shininess)); 		//���淴���ǿ������
  specular = lightSpecular * powerFactor;    				//���㾵��������ǿ��
}

void main()     
{                            		
   gl_Position = mvp_matrix * a_position; //�����ܱ任�������˴λ��ƴ˶���λ��  
   
   vec4 finalAmbient;
   vec4 finalDiffuse;
   vec4 finalSpecular;
   int i = 0;
   for(i = 0; i < 8; i++)
   {
   		if(!light_enable_state[i] > 0)		continue;
   		
		vec4 ambientTemp = vec4(0.0,0.0,0.0,0.0);
		vec4 diffuseTemp = vec4(0.0,0.0,0.0,0.0);
		vec4 specularTemp = vec4(0.0,0.0,0.0,0.0);   
   
   		Light ls = light_state[i];
   		vec4 lposOrg = ls.position;
   		vec3 lpos = vec3(lposOrg.x / lposOrg.w, lposOrg.y / lposOrg.w, lposOrg.z / lposOrg.w);
   		vec4 ambientColor = ls.ambient_color;
   		vec4 diffuseColor = ls.diffuse_color;
   		vec4 specularColor = ls.specular_color;
		pointLight(normalize(a_normal), ambientTemp, diffuseTemp, specularTemp, lpos, ambientColor, diffuseColor, specularColor);
   
		finalAmbient += ambientTemp;
		finalDiffuse += diffuseTemp;
		finalSpecular += specularTemp;
   }
   
   vAmbient = finalAmbient;
   vDiffuse = finalDiffuse;
   vSpecular = finalSpecular;
   
   //��������������괫��ƬԪ��ɫ��
   if(enable_tex[indx_zero] > 0)
   {
		v_texcoord[indx_zero] = a_texCoord0;
   }
   if(enable_tex[indx_one] > 0)
   {
   		v_texcoord[indx_one] = a_texCoord1;
   }
}    
