package Types;

import de.biomedical_imaging.edu.wlu.cs.levy.CG.Editor;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeyDuplicateException;

public class MyEditor extends Editor.Replacer<Integer> {

    public MyEditor(Integer val) {
        super(val);
    }

    @Override
    public Integer edit(Integer current) {
        return super.edit(current);
    }
}
