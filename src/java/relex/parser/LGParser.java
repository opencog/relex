/*
 * Copyright 2009 Borislav Iordanov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package relex.parser;

import org.linkgrammar.LGConfig;

public abstract class LGParser implements IParser
{
	protected LGConfig _config = new LGConfig();
	protected String _dict_path = null;
	protected String _lang = "en";

	public LGConfig getConfig()
	{
		return _config;
	}

	public void setConfig(LGConfig config)
	{
		_config = config;
	}	
	
	public void setDictPath(String path)
	{
		_dict_path = path;
	}	
	
	public void setLanguage(String lang)
	{
		_lang = lang;
	}	
	
	public abstract String getVersion();
}
