package edu.wpi.first.shuffleboard.api.plugin;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;

import com.cedarsoft.version.Version;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * <p>Common superclass for plugins that can be loaded by the app at startup or during runtime. Subclasses must have
 * public no-arg constructor or they will not be loaded.</p>
 *
 * <p>Shuffleboard will load plugins from all jars found in in the {@link Storage#PLUGINS_DIR plugin directory}.
 * This will overwrite pre-existing plugins with the same ID string (eg "edu.wpi.first.shuffleboard.Base") in
 * encounter order, which is alphabetical by jar name. For example, if a jar file "my_plugins.jar" defines a plugin
 * with ID "foo.bar" and another jar file "more_plugins.jar" <i>also</i> defines a plugin with that ID, the plugin
 * from "more_plugins" will be loaded first, then unloaded and replaced with the one from "my_plugins.jar". For this
 * reason, plugin authors should be careful to use unique group IDs. We recommend Java's reverse-DNS naming scheme.
 *
 * <p>Plugins <i>must</i> define their properties (developer group ID, name, current version, and summary) with a
 * {@link Description @Description} annotation. Defining it in an annotation rather than an instance or class method or
 * field allows shuffleboard to get data about the plugin without having to load the class or create a new instance of
 * it. This approach prevents {@link NoClassDefFoundError NoClassDefFoundErrors} and
 * {@link NoSuchMethodError NoSuchMethodErrors} that can arise if a plugin depends on another, or on classes defined in
 * another plugin JAR, that has not been loaded or is not on the classpath.
 *
 * <p>For the same reasons, a plugin that depends on another <i>must</i> provide that information with a
 * {@link Requires @Requires} or {@link Requirements @Requirements} annotation. The former is a <i>repeatable</i>
 * annotation for which the latter is a wrapper; as such, it is recommended to use {@code @Requires} annotations to
 * increase readability.
 */
public class Plugin {

  private final String groupId;
  private final String name;
  private final Version version;
  private final String summary;
  private final BooleanProperty loaded = new SimpleBooleanProperty(this, "loaded", false);

  /**
   * Creates a new plugin instance. The subclass <i>must</i> have a {@link Description @Description} annotation that
   * defines the group ID, name, version, and summary of the plugin. If the plugin depends on another (eg the camera
   * server plugin depends on the network tables one), that should be specified with a {@link Requires @Requires}
   * annotation.
   *
   * @throws InvalidPluginDefinitionException if no {@code @Description} annotation is present, or if the
   */
  protected Plugin() {
    validatePluginClass(getClass());
    Description description = getClass().getAnnotation(Description.class);

    this.groupId = description.group();
    this.name = description.name();
    this.version = Version.parse(description.version());
    this.summary = description.summary();
  }

  /**
   * Validates that a plugin class has valid description and dependency annotations.
   *
   * @param pluginClass the plugin class to validate
   *
   * @throws InvalidPluginDefinitionException if the class has an invalid definition
   */
  public static void validatePluginClass(Class<? extends Plugin> pluginClass) {
    Description description = pluginClass.getAnnotation(Description.class);
    if (description == null) {
      throw new InvalidPluginDefinitionException(
          "The plugin class does not have a @Description annotation: " + pluginClass.getName());
    }
    String group = description.group();
    if (group.contains(":")) {
      throw new InvalidPluginDefinitionException("The group ID cannot contain a colon (:) character: " + group);
    }
    String name = description.name();
    if (name.contains(":")) {
      throw new InvalidPluginDefinitionException("The plugin name cannot contain a colon (:) character: " + name);
    }
    String version = description.version();
    try {
      Version.parse(version);
    } catch (IllegalArgumentException e) {
      throw new InvalidPluginDefinitionException("The version string does not follow semantic versioning", e);
    }
  }

  /**
   * The group ID of this plugin. For example, the stock plugins have the group ID {@code edu.wpi.first.shuffleboard}.
   * This must combine with {@link #getName()} to create a unique identifier for this plugin.
   */
  public final String getGroupId() {
    return groupId;
  }

  /**
   * Gets the name of this plugin.
   */
  public final String getName() {
    return name;
  }

  /**
   * Gets the version of this plugin. This follows the <a href="http://semver.org">semantic versioning</a> scheme.
   */
  public final Version getVersion() {
    return version;
  }

  /**
   * Gets an ID string unique to this plugin in the format {@code "{groupId}:{name}"}.
   */
  public final String idString() {
    return groupId + ":" + name;
  }

  /**
   * Gets an ID string unique to this plugin in the format {@code "{groupId}:{name}:{version}}. For example,
   * "foo.bar:baz:1.0.0".
   */
  public final String fullIdString() {
    return groupId + ":" + name + ":" + version;
  }

  /**
   * Gets a descriptive string describing what this plugin provides.
   */
  public final String getSummary() {
    return summary;
  }

  /**
   * Called when a plugin is loaded. Defaults to do nothing; plugins that require logic to be performed when they're
   * loaded (for example, connecting to a server) should be run here.
   */
  public void onLoad() {
    // Default to NOP
  }

  /**
   * Called when a plugin is unloaded.
   */
  public void onUnload() {
    // Default to NOP
  }

  /**
   * Gets a list of custom data types that this plugin defines.
   */
  public List<DataType> getDataTypes() {
    return ImmutableList.of();
  }

  /**
   * Gets a list of custom source types that this plugin defines.
   */
  public List<SourceType> getSourceTypes() {
    return ImmutableList.of();
  }

  /**
   * Gets a list of the non-annotated components defined by this plugin.
   */
  public List<ComponentType> getComponents() {
    return ImmutableList.of();
  }

  /**
   * Gets a map of the default components to use for each data type.
   */
  public Map<DataType, ComponentType> getDefaultComponents() {
    return ImmutableMap.of();
  }

  public List<TypeAdapter> getTypeAdapters() {
    return ImmutableList.of();
  }

  /**
   * Gets a list of themes that this plugin defines.
   */
  public List<Theme> getThemes() {
    return ImmutableList.of();
  }

  /**
   * Gets a list of properties of this plugin that can be changed by users. Properties that are sensitive to rapid
   * changes (for example, a server URI that will attempt a connection on a change) should be wrapped in a
   * {@link edu.wpi.first.shuffleboard.api.prefs.FlushableProperty FlushableProperty} to ensure that a change will only
   * occur when a user manually confirms the change.
   */
  public List<Property<?>> getProperties() {
    return ImmutableList.of();
  }

  /**
   * Checks if this plugin has been loaded.
   */
  public final boolean isLoaded() {
    return loaded.get();
  }

  public final BooleanProperty loadedProperty() {
    return loaded;
  }

  /**
   * Flags this plugin as loaded or unloaded. <i>This does not perform any loading or unloading</i>, it merely sets the
   * flag. This is used by the plugin loader to set the flag after executing the load/unload logic.
   */
  public final void setLoaded(boolean loaded) {
    this.loaded.set(loaded);
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!obj.getClass().equals(this.getClass())) {
      return false;
    }
    Plugin that = (Plugin) obj;
    return this.groupId.equals(that.groupId)
        && this.name.equals(that.name)
        && this.version.equals(that.version);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(groupId, name, version);
  }

}
