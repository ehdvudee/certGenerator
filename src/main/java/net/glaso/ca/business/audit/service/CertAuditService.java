package net.glaso.ca.business.audit.service;

import net.glaso.ca.business.audit.dao.CertAuditDao;
import net.glaso.ca.business.audit.vo.CertAuditVo;
import net.glaso.ca.business.cert.vo.CertVo;
import net.glaso.ca.business.common.CommonConstants;
import net.glaso.ca.business.common.domain.Criteria;
import net.glaso.ca.business.login.common.LoginConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CertAuditService {

	private final CertAuditDao certAuditDao;

	@Autowired
	public CertAuditService( CertAuditDao certAuditDao ) {
		this.certAuditDao = certAuditDao;
	}

	public List<Map<String, Object>> showList( Criteria cri ) {
		List<Map<String,Object>> voMapList = certAuditDao.selectCertAuditList( cri );

		for ( Map<String, Object> voMap : voMapList ) {
			voMap.put( "date", CommonConstants.dateFormat.format( (Date)voMap.get( "date" ) ) );
		}

		return voMapList;
	}

	public int showListCnt() {
		return certAuditDao.selectCertAuditListCnt();
	}

	public void insertAudit( HttpServletRequest request, CertVo certVo ) throws NoSuchAlgorithmException {
		CertAuditVo vo = new CertAuditVo();

		vo.setResult( 0 );

		insertAudit( request, certVo, vo );
	}

	public void insertAudit( HttpServletRequest request, CertVo certVo, Exception e ) throws NoSuchAlgorithmException {
		CertAuditVo vo = new CertAuditVo();

		vo.setResult( 1 );
		vo.setErrMsg( e.getMessage() );

		insertAudit( request, certVo, vo );
	}

	private void insertAudit( HttpServletRequest request, CertVo certVo, CertAuditVo vo ) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

		vo.setClientIp( request.getRemoteAddr() );
		vo.setServerIp( request.getServerName() );
		vo.setDate( new Date() );
		vo.setUserId( request.getSession().getAttribute( LoginConstants.SESSION_ID ).toString() );
		vo.setAction( certVo.getType() );

		String requestParam = new StringBuilder().append( "type: ").append( certVo.getType() )
				.append( ", ouType: " ).append( certVo.getOuType() )
				.append( ", citeName: " ).append( certVo.getCiteName() )
//				.append( ", validity: " ).append( certVo.getValidity() )
				.append( ", description: ").append( certVo.getDescription() ).toString();

		String hashVal = new StringBuilder().append( vo.getClientIp() )
				.append( vo.getUserId() )
				.append( CommonConstants.dateFormat.format( vo.getDate() ) ).toString();

		vo.setRequestParam( requestParam );
		vo.setHash( messageDigest.digest( hashVal.getBytes() ) );

		certAuditDao.insertCertAudit( vo );
	}
}
