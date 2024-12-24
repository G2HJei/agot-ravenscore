package xyz.zlatanov.ravenscore.model.tourdetails;

import static xyz.zlatanov.ravenscore.Utils.capitalizeFirstLetter;

import java.net.URI;

import com.google.common.net.InternetDomainName;

import lombok.Data;
import lombok.val;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class ProfileLink {

	private String	label;
	private String	link;

	public ProfileLink(String link) {
		try {
			val tpd = InternetDomainName.from(new URI(link).getHost())
					.topPrivateDomain();
			val tpdStr = tpd.toString().toLowerCase();
			val suffix = "." + tpd.publicSuffix().toString();
			val simpleDomain = tpdStr.substring(0, tpdStr.indexOf(suffix));
			label = switch (simpleDomain) {
				case "swordsandravens" -> "SnR";
				default -> capitalizeFirstLetter(simpleDomain);
			};
			this.link = link;
		} catch (Exception e) {
			label = link; // discord handle or anything else that is not a hyperlink
		}
	}
}
