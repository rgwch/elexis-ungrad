package ch.elexis.ungrad.qrbills;

import java.util.List;

import ch.fd.invoice450.request.ServiceExType;
import ch.fd.invoice450.request.ServiceType;
import ch.rgw.tools.Money;

public class XML45Services {

	private Money tarmedMoney=new Money();
	private Money drugMoney=new Money();
	private Money drgMoney=new Money();
	private Money migelMoney=new Money();
	private Money labMoney=new Money();
	private Money paramedMoney=new Money();
	private Money otherMoney=new Money();
	private Money complementaryMoney=new Money();
	private List<Object> services;

	public XML45Services(ch.fd.invoice450.request.ServicesType services) {
		this.services = services.getServiceExOrService();
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
