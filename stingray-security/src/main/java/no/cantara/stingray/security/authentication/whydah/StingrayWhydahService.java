package no.cantara.stingray.security.authentication.whydah;

import net.whydah.sso.user.types.UserToken;
import no.cantara.stingray.security.authentication.StingrayApplicationTag;

import java.util.List;

public interface StingrayWhydahService {

    UserToken findUserTokenFromUserTokenId(String userTokenId);

    String getUserTokenByUserTicket(String userticket);

    String getApplicationIdFromApplicationTokenId(String applicationTokenId);

    List<StingrayApplicationTag> getApplicationTagsFromApplicationTokenId(String applicationTokenId);

    String createTicketForUserTokenID(String userTokenId);

    boolean validateUserTokenId(String usertokenid);
}
