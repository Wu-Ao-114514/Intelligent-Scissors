package W10;

import com.sun.jdi.Value;
import java.security.Key;

public class TreeNode {
    public Key key;
    public Value value;
    public TreeNode left = null;
    public TreeNode right = null;
    public TreeNode parent = null;
    public TreeNode( Key key, Value value ) {
        this.key = key;
        this.value = value;
    }
}