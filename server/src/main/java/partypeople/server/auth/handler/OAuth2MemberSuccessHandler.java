package partypeople.server.auth.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import partypeople.server.auth.filter.JwtAuthenticationFilter;
import partypeople.server.auth.jwt.JwtTokenizer;
import partypeople.server.auth.userdetails.MemberDetailService;
import partypeople.server.auth.utils.CustomAuthorityUtils;
import partypeople.server.exception.BusinessLogicException;
import partypeople.server.member.entity.Member;
import partypeople.server.member.service.MemberService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class OAuth2MemberSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final MemberService memberService;
    private Boolean googleJoin;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        var oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = String.valueOf(oAuth2User.getAttributes().get("email"));

        googleJoin = false;

        Member member;
        try {
            member = memberService.findMember(email);
        } catch (BusinessLogicException e) {
            member = saveMember(oAuth2User);
        }

        String accessToken = jwtTokenizer.delegateAccessToken(member);
        String refreshToken = jwtTokenizer.delegateRefreshToken(member);

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh", refreshToken);

        redirect(request, response, accessToken, refreshToken, member.getRoles());
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response, String accessToken, String refreshToken, List<String> authorities) throws IOException {
        String uri = createURI(accessToken, refreshToken).toString();
        getRedirectStrategy().sendRedirect(request, response, uri);
    }

    private URI createURI(String accessToken, String refreshToken) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

        queryParams.add("access_token", accessToken);
        queryParams.add("refresh_token", refreshToken);
        queryParams.add("google_join", googleJoin.toString());

        return UriComponentsBuilder
            .newInstance()
            .scheme("http")
            .host("partypeople.s3-website.ap-northeast-2.amazonaws.com")
//            .port(3000)
            .path("/login")
            .queryParams(queryParams)
            .build()
            .toUri();
    }

    private Member saveMember(OAuth2User oAuth2User) {
        String email = String.valueOf(oAuth2User.getAttributes().get("email"));
        String profile = String.valueOf(oAuth2User.getAttributes().get("picture"));
        String name = String.valueOf(oAuth2User.getAttributes().get("name"));
        String password = memberService.generateRandomPassword();
        List<String> authorities = authorityUtils.createRoles(email);

        Member member = new Member();
        member.setGender("NONE");
        member.setEmail(email);
        member.setRoles(authorities);
        member.setPassword(password);
        member.setNickname(memberService.oauthNickCheck(name));
        member.setProfile(profile);

        googleJoin = true;

        Member saveMember = memberService.createMember(member);
        memberService.oauthPassword(email, password);

        return saveMember;
    }

}
