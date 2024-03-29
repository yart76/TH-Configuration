package com.company.hudson.plugins;
import hudson.DescriptorExtensionList;
import hudson.Launcher;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link HelloWorldBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)} method
 * will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class ThConfigurationBuilder extends Builder {

    private final String name;
    private final Fruit fruit;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ThConfigurationBuilder(String name, Fruit fruit) {
        this.name = name;
        this.fruit = fruit;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getName() {
        return name;
    }
    public Fruit getFruit() {
        return fruit;
    }

    public static class Fruit implements ExtensionPoint, Describable<Fruit> {
        protected String name;
        private Fruit(String name) {
            this.name = name;
        }
        public Descriptor<Fruit> getDescriptor() {
            return Hudson.getInstance().getDescriptor(getClass());
        }
    }

    public static class FruitDescriptor extends Descriptor<Fruit> {
        public FruitDescriptor(Class<? extends Fruit> clazz) {
            super(clazz);
        }
        public String getDisplayName() {
            return clazz.getSimpleName();
        }
    }

    public static class Apple extends Fruit {
        private int seeds;
        @DataBoundConstructor public Apple(int seeds) {
            super("Apple");
            this.seeds = seeds;
        }
        public int getSeeds() {
            return seeds;
        }
        @Extension
        public static final FruitDescriptor D = new FruitDescriptor(Apple.class);
    }

    public static class Banana extends Fruit {
        private boolean yellow;
        @DataBoundConstructor public Banana(boolean yellow) {
            super("Banana");
            this.yellow = yellow;
        }
        public boolean isYellow() {
            return yellow;
        }
        @Extension
        public static final FruitDescriptor D = new FruitDescriptor(Banana.class);
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        // this is where you 'build' the project
        // since this is a dummy, we just say 'hello world' and call that a build

        // this also shows how you can consult the global configuration of the builder
        if(getDescriptor().useFrench())
            listener.getLogger().println("Bonjour, "+name+"!");
        else
            listener.getLogger().println("Hello, "+name+"!");
        return true;
    }

    // overrided for better type safety.
    // if your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>views/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // this marker indicates Hudson that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private boolean useFrench;

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException {
            if(value.length()==0)
                return FormValidation.error("Please set a name");
            if(value.length()<4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "TH Configuration";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            useFrench = formData.getBoolean("useFrench");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         */
        public boolean useFrench() {
            return useFrench;
        }

        public DescriptorExtensionList<Fruit,Descriptor<Fruit>> getFruitDescriptors() {
            return Hudson.getInstance().<Fruit,Descriptor<Fruit>>getDescriptorList(Fruit.class);
        }
    }
}

