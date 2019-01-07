package ch.so.agi.gretl.tasks;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

public class Ili2pgUpdate extends Ili2pgAbstractTask {
    @InputFile
    public Object dataFile = null;

    @TaskAction
    public void updateData() {
        Config settings = createConfig();
        int function = Config.FC_UPDATE;
        if (dataFile == null) {
            return;
        }
        String xtfFilename = this.getProject().file(dataFile).getPath();
        if (Ili2db.isItfFilename(xtfFilename)) {
            settings.setItfTransferfile(true);
        }
        settings.setXtffile(xtfFilename);
        run(function, settings);
    }
}

