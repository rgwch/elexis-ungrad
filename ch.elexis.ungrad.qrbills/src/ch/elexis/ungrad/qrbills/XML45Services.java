package ch.elexis.ungrad.qrbills;

import java.util.List;

import ch.fd.invoice450.request.ServiceExType;
import ch.fd.invoice450.request.ServiceType;
import ch.rgw.tools.Money;

public class XML45Services {

	private Money tarmedMoney;
	private Money drugMoney;
	private Money drgMoney;
	private Money migelMoney;
	private Money labMoney;
	private Money paramedMoney;
	private Money otherMoney;
	private Money complementaryMoney;
	private List<Object> services;

	public XML45Services(ch.fd.invoice450.request.ServicesType services) {
		this.services = services.getServiceExOrService();
		drgMoney = new Money();
		paramedMoney = new Money();
		otherMoney = new Money();
		complementaryMoney = new Money();
		initMoneyAmounts();
	}

	private void initMoneyAmounts() {
		for (Object rec : services) {
			if (rec instanceof ServiceExType) {
				tarmedMoney.addAmount(((ServiceExType) rec).getAmount());
			} else {
				ServiceType sr = (ServiceType) rec;
				String type = sr.getTariffType();
				switch (type) {
				case "317":
					labMoney.addAmount(sr.getAmount());
					break;
				case "452":
				case "454":
					migelMoney.addAmount(sr.getAmount());
					break;
				case "402":
				case "403":
					drugMoney.addAmount(sr.getAmount());
					break;
				case "401":
					paramedMoney.addAmount(sr.getAmount());
					break;
				case "590":
					complementaryMoney.addAmount(sr.getAmount());
					break;
				default:
					otherMoney.addAmount(sr.getAmount());
				}
			}

		}
	}

	public Money getTarmedMoney() {
		return tarmedMoney;
	}

	public Money getDrugMoney() {
		return drugMoney;
	}

	public Money getDrgMoney() {
		return drgMoney;
	}

	public Money getMigelMoney() {
		return migelMoney;
	}

	public Money getLabMoney() {
		return labMoney;
	}

	public Money getParamedMoney() {
		return paramedMoney;
	}

	public Money getOtherMoney() {
		return otherMoney;
	}

	public Money getComplementaryMoney() {
		return complementaryMoney;
	}
}
