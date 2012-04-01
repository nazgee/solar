package eu.nazgee.simple;

import eu.nazgee.game.utils.loadable.LoadableResource;
import eu.nazgee.game.utils.loadable.LoaderResource;

/**
 * Base class for all loadable Resources. Creates an instance of loader than
 * allows installing other loadable resources.
 * 
 * @author nazgee
 *
 */
public abstract class Resources extends LoadableResource {
	private final LoaderResource mLoader = new LoaderResource(this);

	public Resources() {
		super();
	}

	@Override
	public LoaderResource getLoader() {
		return mLoader;
	}
}