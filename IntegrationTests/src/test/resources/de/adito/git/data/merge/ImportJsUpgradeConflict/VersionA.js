import { result  } from "@aditosoftware/jdito-types";
import { vars } from "@aditosoftware/jdito-types";
import { KeywordUtils, KeywordAttribute, LanguageKeywordUtils, CurrencyKeywordUtils } from "Keyword_lib";
import { $KeywordRegistry } from "KeywordRegistry_basic";

result.string(KeywordUtils.getViewValue($KeywordRegistry.activityCategory(), vars.get("$field.CATEGORY")));