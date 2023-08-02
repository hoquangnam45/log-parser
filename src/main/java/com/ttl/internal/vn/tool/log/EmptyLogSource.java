package com.ttl.internal.vn.tool.log;

import lombok.NoArgsConstructor;

import java.util.Iterator;

@NoArgsConstructor
public class EmptyLogSource implements ILogSource
{
	@Override
	public String getID()
	{
		return null;
	}

	@Override
	public void refresh()
	{
		/* noop */
	}

	@Override
	public ILogSource add(ILogSource anotherSource)
	{
		if (!anotherSource.iterator().hasNext()) {
			return new EmptyLogSource();
		} else {
			return anotherSource.clone();
		}
	}

	@Override
	public Iterator<ILogEntry> iterator()
	{
		return new Iterator<>()
		{
			@Override
			public boolean hasNext()
			{
				return false;
			}

			@Override
			public ILogEntry next()
			{
				return null;
			}
		};
	}
}
