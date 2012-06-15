precision mediump float;

#define NUM_TEXTURES				2

varying	vec4	v_texcoord[NUM_TEXTURES];
varying vec4	v_front_color;
varying vec4	v_back_color;
varying float	v_fog_factor;
varying float	v_ucp_factor;

varying vec4 vAmbient;
varying vec4 vDiffuse;
varying vec4 vSpecular;

uniform sampler2D sTexture_0;			//纹理内容数据0
uniform sampler2D sTexture_1;			//纹理内容数据1

uniform bool	enable_tex[NUM_TEXTURES];		//texture enables

int indx_zero = 0;
int indx_one = 1;

void main()
{
	vec4 clr_0;
	vec4 clr_1;
	
	if(enable_tex[indx_zero] || enable_tex[indx_one])
	{
		if(enable_tex[indx_zero])
		{
			clr_0 = texture2D(sTexture_0, v_texcoord[indx_zero]);
			gl_FragColor = clr_0 * vAmbient + clr_0 * vDiffuse + clr_0 * vSpecular;
		}
		else if(enable_tex[indx_one])
		{
			clr_1 = texture2D(sTexture_1, v_texcoord[indx_one]);
			gl_FragColor = clr_0 * vAmbient + clr_0 * vDiffuse + clr_0 * vSpecular;
		}
		else
		{
			clr_0 = texture2D(sTexture_0, v_texcoord[indx_zero]);
			clr_1 = texture2D(sTexture_1, v_texcoord[indx_one]);
			if(vDiffuse.x > 0.21)
			{
				gl_FragColor = clr_0;    
			} 
			else if(vDiffuse.x < 0.05)
			{     
			  	gl_FragColor = clr_1;
			}
			else
			{
			    float t = (vDiffuse.x - 0.05) / 0.16;
			    gl_FragColor = t * clr_0 + (1.0 - t) * clr_1;
			}  
		}
		return;
	}
	
	if(gl_FrontFacing)
	{
		gl_FragColor = v_front_color;
	}
	else
	{
		gl_FragColor = v_back_color;
	}
}