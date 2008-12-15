package relex.parser;

import org.linkgrammar.LGConfig;

public abstract class LGParser implements IParser
{
	protected LGConfig config = new LGConfig();

	public LGConfig getConfig()
	{
		return config;
	}

	public void setConfig(LGConfig config)
	{
		this.config = config;
	}	
	
	public abstract String getVersion();
}
