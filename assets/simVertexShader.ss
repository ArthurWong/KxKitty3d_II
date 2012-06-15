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

//定位光光照计算的方法
void pointLight(				//定位光光照计算的方法
  in vec3 normal,				//法向量
  inout vec4 ambient,			//环境光最终强度
  inout vec4 diffuse,			//散射光最终强度
  inout vec4 specular,			//镜面光最终强度
  in vec3 lightLocation,		//光源位置
  in vec4 lightAmbient,			//环境光强度
  in vec4 lightDiffuse,			//散射光强度
  in vec4 lightSpecular			//镜面光强度
)
{
  //计算环境光强度
  ambient = lightAmbient;						//直接得出环境光的最终强度  
  
  //计算散射光强度
  vec3 normalTarget = aPosition+normal;			//计算变换后的法向量
  vec3 newNormal = (uMMatrix * vec4(normalTarget, 1)).xyz - (uMMatrix * a_position).xyz;
  newNormal = normalize(newNormal); 			//对法向量规格化
  //计算从表面点到摄像机的向量
  vec3 eye = normalize(uCamera - (uMMatrix * a_position).xyz);  
  //计算从表面点到光源位置的向量vp
  vec3 vp = normalize(lightLocation - (uMMatrix * a_position).xyz);  
  vp = normalize(vp);//格式化vp
  vec3 halfVector = normalize(vp + eye);		//求视线与光线的半向量    
  float shininess = 50.0;						//粗糙度，越小越光滑
  float nDotViewPosition = max(0.0, dot(newNormal, vp)); 	//求法向量与vp的点积与0的最大值
  diffuse = lightDiffuse * nDotViewPosition;				//计算散射光的最终强度
  
  //计算反射光强度
  float nDotViewHalfVector = dot(newNormal, halfVector);	//法线与半向量的点积 
  float powerFactor = max(0.0, pow(nDotViewHalfVector, shininess)); 		//镜面反射光强度因子
  specular = lightSpecular * powerFactor;    				//计算镜面光的最终强度
}

void main()     
{                            		
   gl_Position = mvp_matrix * a_position; //根据总变换矩阵计算此次绘制此顶点位置  
   
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
   
   //将顶点的纹理坐标传给片元着色器
   if(enable_tex[indx_zero] > 0)
   {
		v_texcoord[indx_zero] = a_texCoord0;
   }
   if(enable_tex[indx_one] > 0)
   {
   		v_texcoord[indx_one] = a_texCoord1;
   }
}    
