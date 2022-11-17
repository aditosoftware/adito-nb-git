import("system.result");
import("system.vars");
import("Keywrd_lib");
import("KeywordRegistry_basic");

result.string(KeywordUtils.getViewValue($KeywordRegistry.activityCategory(), vars.get("$field.CATEGORY")));