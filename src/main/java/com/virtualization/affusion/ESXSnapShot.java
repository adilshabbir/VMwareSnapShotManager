package com.virtualization.affusion;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;


import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link ESXSnapShot} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author adil
 */
public class ESXSnapShot extends Builder {

	private final String operationType;
    private final String vCenterServerIP;
    private final String vmSnapShotName;
    private final String userID;
    private final String password;
    private final String vmName;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ESXSnapShot(String operationType,String vCenterServerIP, String vmSnapShotName, String userID,String password, String vmName) {
    	this.operationType = operationType;
        this.vCenterServerIP = vCenterServerIP;
        this.vmSnapShotName = vmSnapShotName;
        this.userID = userID;
        this.password = password;
        this.vmName= vmName;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getOperationType() {
        return operationType;
    }
    
    public String getvCenterServerIP() {
		return vCenterServerIP;
	}
    
	public String getVmSnapShotName() {
		return vmSnapShotName;
	}

	public String getUserID() {
		return userID;
	}

	public String getPassword() {
		return password;
	}
	
	public String getVmName() {
		return vmName;
	}

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        // This is where you 'build' the project.
        // Since this is a dummy, we just say 'hello world' and call that a build.

        // This also shows how you can consult the global configuration of the builder
		//listener.getLogger().println("Reverting Snapshot::"+vmName+":"+vmSnapShotName);
		
		VMSnapshotUtil snapShot = new VMSnapshotUtil();
        try {
        	listener.getLogger().println("Virtual Machine Name::"+vmName);
        	listener.getLogger().println("SnapShot::"+vmSnapShotName);
        	listener.getLogger().println("Operation Name SnapShot::"+operationType);
        	
			snapShot.snapShotManager(vCenterServerIP,vmName,vmSnapShotName,userID,password,operationType,listener);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(listener.getLogger());
			return false;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(listener.getLogger());
			return false;
			
		}
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link ESXSnapShot}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
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
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
           /* if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");*/
            return FormValidation.ok();
        }
        
		public FormValidation doCheckUserID(@QueryParameter String value)
			   throws IOException, ServletException {
				if (value.length() == 0)
			return FormValidation.error("Please set a User ID");
				/* if (value.length() < 4)
			return FormValidation.warning("Isn't the name too short?");*/
			    return FormValidation.ok();
		}
		
		public FormValidation doCheckVCenterServerIP(@QueryParameter String value)
		   throws IOException, ServletException {
			if (value.length() == 0)
		return FormValidation.error("Please set Virtual Center Server IP");
			
		    return FormValidation.ok();
	}
		public FormValidation doCheckVmSnapShotName(@QueryParameter String value)
		   throws IOException, ServletException {
			if (value.length() == 0)
		return FormValidation.error("Please set Snap Shot Name");
			
		    return FormValidation.ok();
	}
		public FormValidation doCheckPassword(@QueryParameter String value)
		   throws IOException, ServletException {
			if (value.length() == 0)
		return FormValidation.error("Please set Password");
			
		    return FormValidation.ok();
	}
		public FormValidation doCheckVmName(@QueryParameter String value)
		   throws IOException, ServletException {
			if (value.length() == 0)
		return FormValidation.error("Please set Virtual Machine Name");
			
		    return FormValidation.ok();
	}
		public FormValidation doTestConnection(@QueryParameter("vCenterServerIP") final String vCenterServerIP, @QueryParameter("userID") final String userID, @QueryParameter("password") final String password ,@QueryParameter("vmName") final String vmName , @QueryParameter("vmSnapShotName") final String vmSnapShotName) throws IOException, ServletException {
		    try {
		    	ServiceInstance si = new ServiceInstance(new URL("https://"+vCenterServerIP+"/sdk"), userID, password, true);
				long end = System.currentTimeMillis();
				
				Folder rootFolder = si.getRootFolder();
				String name = rootFolder.getName();
				
				ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
				 
				if(mes==null || mes.length ==0)
				{
					return FormValidation.error("Error : No Managed Entities of type VirtualMachine found");
				}
				 VirtualMachine vm = (VirtualMachine) new InventoryNavigator(
					      rootFolder).searchManagedEntity("VirtualMachine", vmName);
		        return FormValidation.ok("Connection Success");
		    } catch (Exception e) {
		        return FormValidation.error("Connection Error");
		    }
		}
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Virtual Machine Snapshot Manager";
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
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        public boolean getUseFrench() {
            return useFrench;
        }
    }
}

