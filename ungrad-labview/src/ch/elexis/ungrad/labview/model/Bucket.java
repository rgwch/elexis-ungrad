package ch.elexis.ungrad.labview.model;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.data.Patient;
import ch.rgw.tools.StringTool;

public class Bucket {
	final String ntyp = Integer.toString(LabItemTyp.NUMERIC.ordinal());

	Item item;
	Patient pat;
	List<Result> results = new ArrayList<Result>();
	static final int LOWER_BOUND_MALE = 0;
	static final int UPPER_BOUND_MALE = 1;
	static final int LOWER_BOUND_FEMALE = 2;
	static final int UPPER_BOUND_FEMALE = 3;

	float[] refBounds = new float[4];

	Bucket(Patient patient, Item item) {
		this.item = item;
		pat = patient;
		if (item.type.equals(ntyp)) {
			makeBounds(item.refMann, 0);
			makeBounds(item.refFrauOrTx, 2);
		}
	}

	private void makeBounds(String val, int index) {
		if (!StringTool.isNothing(val)) {
			String chopped = val.replaceAll("\\s", "");
			if (chopped.startsWith("<")) {
				refBounds[index] = 0;
				refBounds[index + 1] = makeFloat(chopped.substring(1));
			} else if (chopped.startsWith(">")) {
				refBounds[index] = makeFloat(chopped.substring(1));
				refBounds[index+1]= Float.MAX_VALUE;
			} else {
				String[] bounds = chopped.split("-");
				if (bounds.length > 0) {
					refBounds[index] = makeFloat(bounds[0]);
					if (bounds.length == 2) {
						refBounds[index + 1] = makeFloat(bounds[1]);
					}
				}
			}
		}
	}

	void addResult(Result result) {
		results.add(result);
	}

	public int getResultCount() {
		return results.size();
	}

	public String getMinResult() {
		float cmp = Float.MAX_VALUE;
		for (Result result : results) {
			float val = makeFloat(result.result);
			if (val < cmp) {
				cmp = val;
			}
		}
		return Float.toString(cmp);
	}

	public String getMaxResult() {
		float cmp = 0;
		for (Result result : results) {
			float val = makeFloat(result.result);
			if (val > cmp) {
				cmp = val;
			}
		}
		return Float.toString(cmp);
	}

	public String getAverageResult() {
		int num = 0;
		float sum = 0;
		for (Result result : results) {
			sum += makeFloat(result.result);
			num += 1;
		}
		float avg = 100 * sum / num;
		float ret = ((float) Math.round(avg)) / 100f;
		return Float.toString(ret);
	}

	float makeFloat(String s){
		if(s.startsWith("<")){
			return makeFloatInternal(s.substring(1).trim());
		}else if(s.startsWith(">")){
			return makeFloatInternal(s.substring(1).trim());
		}else{
			return makeFloatInternal(s);
		}
	}
	private float makeFloatInternal(String s) {
		String[] splitted = s.split("[\\.,]");
		if (splitted[0].matches("\\s*[0-9]+\\s*")) {
			String einer = splitted[0].trim();
			String frac = "0";
			if (splitted.length > 2) {
				return -1.0f;
			}
			if (splitted.length == 2) {
				if (splitted[1].matches("\\s*[0-9]+\\s*")) {
					frac = splitted[1].trim();
				} else {
					return -1.0f;
				}
			}
			frac = StringTool.pad(StringTool.RIGHTS, '0', frac, 3);
			float fr = ((float) Integer.parseInt(frac)) / 1000f;
			float ret = Integer.parseInt(einer) + fr;
			return ret;
		} else {
			return -1.0f;
		}
	}

	public boolean isPathologic(String value) {
		String chopped = value.trim();
		float val = 0f;
		if (chopped.startsWith("<")) {
			val = 0;
		} else if (chopped.startsWith(">")) {
			val = Integer.MAX_VALUE;
		} else {
			val = makeFloat(chopped);
		}
		int index = 0;
		if (pat.getGender() == Gender.FEMALE) {
			index = 2;
		}
		if ((val < refBounds[index]) || (val > refBounds[index + 1])) {
			return true;
		} else {
			return false;
		}
	}
}
