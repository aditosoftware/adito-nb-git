import { KeywordUtils, KeywordAttribute, LanguageKeywordUtils, CurrencyKeywordUtils } from "Keyword_lib";
import { $KeywordRegistry } from "KeywordRegistry_basic";
import { result, vars } from "@aditosoftware/jdito-types";

result.string(KeywordUtils.getViewValue($KeywordRegistry.activityCategory(), vars.get("$field.CATEGORY")));