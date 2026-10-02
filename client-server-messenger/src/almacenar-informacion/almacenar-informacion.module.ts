import { Module } from '@nestjs/common';
import { AlmacenarInformacionService } from './almacenar-informacion.service.js';

@Module({
  providers: [AlmacenarInformacionService]
})
export class AlmacenarInformacionModule {}
