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

/**
 * Represents an unrecoverable error during parsing. Such exceptions
 * are caused by improper configuration, a bug, a failure to access
 * some resource such as a remote server etc.
 *
 * @author Borislav Iordanov
 */
public class ParseException extends RuntimeException
{
	private static final long serialVersionUID = -1;
	
	public ParseException(String msg) { super(msg); }
	public ParseException(String msg, Throwable cause) { super(msg, cause); }
}
