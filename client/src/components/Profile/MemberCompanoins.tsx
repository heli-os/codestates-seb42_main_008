import {
  MemberCompanionsProps,
  MyCompanion,
} from 'interfaces/Profile.interface';
import { useEffect, useState } from 'react';
import styled from 'styled-components';
import ListComponent from './ListComponent';
import customAxios from 'api/customAxios';

const MemberCompanoins = ({ member, user }: MemberCompanionsProps) => {
  const [subscribers, setSubscribers] = useState<MyCompanion[] | []>([]);
  const [participants, setParticipants] = useState<MyCompanion[] | []>([]);
  const [writters, setWritters] = useState<MyCompanion[] | []>([]);
  const [titleHead, setTitleHead] = useState<string>('');

  const getData = async (dataType: string) => {
    await customAxios.get(`/${dataType}`).then(resp => {
      if (dataType === 'subscribers') {
        setSubscribers(resp.data);
      } else if (dataType === 'participants') {
        setParticipants(resp.data);
      } else {
        setWritters(resp.data);
      }
    });
  };

  useEffect(() => {
    getData('subscribers');
    getData('participants');
    getData('writters');
    setTitleHead(cur => {
      if (member) {
        return member.memberId === user.memberId
          ? '내가 '
          : `${member.nickname}님이 `;
      }
      return cur;
    });
  }, [user]);

  return (
    <CompanoinsWrapper>
      {writters.length !== 0 && (
        <ListComponent
          datas={writters}
          titleHead={titleHead}
          titleBody="작성한 "
        />
      )}
      {participants.length !== 0 && (
        <ListComponent
          datas={participants}
          titleHead={titleHead}
          titleBody="참여한 "
        />
      )}
      {subscribers.length !== 0 && (
        <ListComponent
          datas={subscribers}
          titleHead={titleHead}
          titleBody="신청한 "
        />
      )}
    </CompanoinsWrapper>
  );
};

const CompanoinsWrapper = styled.section`
  width: 100%;
  padding: 20px 0;
  padding-top: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 30px;
`;

export default MemberCompanoins;