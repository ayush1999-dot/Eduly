import { ROLE } from './constant';

export interface Tutors {
  tutors: Tutor[];
}

export interface Tutor {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  status: string;
  pricing: number;
  rating: number;
  numberOfRatings: number;
  topic: string[];
  language: string;
  experience: number;
  intro: string;
  numLessonsTaught: number;
  profileImgLink:
    | {
        link: string;
        status: string;
      }
    | string;
  cv: {
    link: string;
    status: string;
  };
  video: {
    link: string;
    status: string;
  };
  bbbLink: string;
  schedule: Schedule[];
  reviews: Review[];
  videoLink: string;
  cvLink: string;
}

export interface UserInfo {
  id: number;
  name: string;
  token: string;
  role: ROLE;
  profileImgLink: string;
}
export interface Conversations {
  conversations: Conversation[];
}

export interface Conversation {
  chatId: string | undefined;
  lastMessageContent: string;
  name: string;
  profileImgLink: string;
  userId: number;
}

export interface Chats {
  messages: Message[];
}

export interface Message {
  messageId: number;
  content: string;
  timestamp: string;
  senderName: string;
  senderId: number;
  senderRole: ROLE;
  chatId: number;
}

export interface Review {
  id: number;
  rating: number;
  text: string;
  reviewBy: {
    name: string;
    profileImgLink: string;
  };
}

export interface ScheduleTiming {
  sessionId: number;
  from: string;
  to: string;
  status: string;
}

export interface Schedule {
  date: string;
  timings: ScheduleTiming[];
}

export interface Student {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  profileImgLink: string;
}

export interface FetchTopics {
  topics: string[];
}

export interface FetchLanguages {
  langauges: string[];
}
